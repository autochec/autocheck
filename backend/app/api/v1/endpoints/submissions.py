"""
SubmissionController — загрузка решений, статусы, результаты, вердикт, AI-анализ.
Все вызовы к БД и бизнес-логике — только через use cases и репозитории (абстракции).

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from pydantic import BaseModel

from app.core.response import ResponseWrapper
from app.core.security import get_current_user_id
from app.infrastructure.di import (
    get_submit_use_case,
    get_submission_repo,
    get_result_repo,
)
from app.application.use_cases.submit_assignment import SubmitAssignmentUseCase
from app.infrastructure.repositories.submission_repository import (
    SubmissionRepository, CheckResultRepository,
)

logger = logging.getLogger(__name__)
router = APIRouter()


class SubmitGitRequest(BaseModel):
    assignment_id: str
    candidate_name: str
    candidate_email: str
    git_url: str


class VerdictRequest(BaseModel):
    verdict: str      # "accepted" | "rejected"
    comment: str = ""


@router.post("", status_code=201)
async def submit_git(
    body: SubmitGitRequest,
    user_id: str = Depends(get_current_user_id),
    use_case: SubmitAssignmentUseCase = Depends(get_submit_use_case),
) -> ResponseWrapper:
    """Загружает решение через Git URL и ставит задачу в очередь Celery."""
    logger.info(
        f"[SubmissionController]: Загрузка Git — "
        f"assignment={body.assignment_id}, candidate={body.candidate_email}"
    )
    try:
        submission = await use_case.execute(
            assignment_id=body.assignment_id,
            candidate_id=user_id,
            source_type="git",
            source_path=body.git_url,
        )
        return ResponseWrapper.ok(data={
            "id": submission.id,
            "status": submission.status.value,
        })
    except Exception as e:
        logger.error(f"[SubmissionController]: Ошибка submit — {e}")
        raise HTTPException(status_code=500, detail="Ошибка загрузки решения")


@router.post("/upload", status_code=201)
async def submit_zip(
    assignment_id: str,
    file: UploadFile = File(...),
    user_id: str = Depends(get_current_user_id),
    use_case: SubmitAssignmentUseCase = Depends(get_submit_use_case),
) -> ResponseWrapper:
    """Загружает ZIP-архив решения (до 50 МБ) и ставит задачу в очередь."""
    MAX_SIZE = 50 * 1024 * 1024

    if not (file.filename or "").endswith(".zip"):
        raise HTTPException(status_code=422, detail="Допустимый формат файла: .zip")

    content = await file.read()
    if len(content) > MAX_SIZE:
        raise HTTPException(status_code=422, detail="Файл превышает максимальный размер 50 МБ")

    save_dir = f"/tmp/submissions/{user_id}"
    os.makedirs(save_dir, exist_ok=True)
    save_path = f"{save_dir}/{assignment_id}.zip"
    with open(save_path, "wb") as f:
        f.write(content)

    logger.info(
        f"[SubmissionController]: Загрузка ZIP — "
        f"assignment={assignment_id}, size={len(content)//1024}KB"
    )
    submission = await use_case.execute(
        assignment_id=assignment_id,
        candidate_id=user_id,
        source_type="zip",
        source_path=save_path,
    )
    return ResponseWrapper.ok(data={
        "id": submission.id,
        "status": submission.status.value,
    })


@router.get("")
async def list_submissions(
    user_id: str = Depends(get_current_user_id),
    repo: SubmissionRepository = Depends(get_submission_repo),
) -> ResponseWrapper:
    """Возвращает список всех проверок."""
    items = await repo.get_all()
    return ResponseWrapper.ok(data=[
        {"id": s.id, "status": s.status.value,
         "score": s.total_score, "assignment_id": s.assignment_id}
        for s in items
    ])


@router.get("/{submission_id}")
async def get_submission(
    submission_id: str,
    repo: SubmissionRepository = Depends(get_submission_repo),
) -> ResponseWrapper:
    """Возвращает детальную информацию о проверке."""
    s = await repo.get_by_id(submission_id)
    if not s:
        raise HTTPException(status_code=404, detail="Проверка не найдена")
    return ResponseWrapper.ok(data={
        "id": s.id, "status": s.status.value,
        "score": s.total_score, "verdict": s.verdict,
        "assignment_id": s.assignment_id,
    })


@router.get("/{submission_id}/status")
async def get_status(
    submission_id: str,
    repo: SubmissionRepository = Depends(get_submission_repo),
) -> ResponseWrapper:
    """Возвращает текущий статус проверки: pending | running | done | error."""
    s = await repo.get_by_id(submission_id)
    if not s:
        raise HTTPException(status_code=404, detail="Проверка не найдена")
    return ResponseWrapper.ok(data={"status": s.status.value, "score": s.total_score})


@router.get("/{submission_id}/results")
async def get_results(
    submission_id: str,
    repo: CheckResultRepository = Depends(get_result_repo),
) -> ResponseWrapper:
    """Возвращает результаты всех чекеров для проверки."""
    results = await repo.get_by_submission(submission_id)
    return ResponseWrapper.ok(data=[
        {
            "checker": r.checker,
            "status": r.status.value,
            "score": r.score,
            "message": r.message,
            "details": r.details,
        }
        for r in results
    ])


@router.post("/{submission_id}/rerun")
async def rerun(
    submission_id: str,
    user_id: str = Depends(get_current_user_id),
    repo: SubmissionRepository = Depends(get_submission_repo),
) -> ResponseWrapper:
    """Запускает повторную проверку решения."""
    logger.info(
        f"[SubmissionController]: Повторный запуск — submissionId={submission_id}"
    )
    s = await repo.get_by_id(submission_id)
    if not s:
        raise HTTPException(status_code=404, detail="Проверка не найдена")

    from worker.tasks import run_checks_task
    run_checks_task.delay(submission_id)
    return ResponseWrapper.ok(data={"status": "pending"})


@router.put("/{submission_id}/verdict")
async def verdict(
    submission_id: str,
    body: VerdictRequest,
    user_id: str = Depends(get_current_user_id),
    repo: SubmissionRepository = Depends(get_submission_repo),
) -> ResponseWrapper:
    """Эксперт выносит вердикт: accepted или rejected."""
    logger.info(
        f"[SubmissionController]: Вердикт — "
        f"submissionId={submission_id}, verdict={body.verdict}, expertId={user_id}"
    )
    if body.verdict not in ("accepted", "rejected"):
        raise HTTPException(status_code=422, detail="Вердикт: accepted или rejected")
    await repo.update_verdict(submission_id, body.verdict)
    return ResponseWrapper.ok(data={"verdict": body.verdict})


@router.get("/{submission_id}/ai-review")
async def ai_review(
    submission_id: str,
    repo: SubmissionRepository = Depends(get_submission_repo),
) -> ResponseWrapper:
    """Возвращает AI-анализ кода (кэшируется в БД после первого запроса)."""
    from app.application.use_cases.ai_review import AICodeReviewUseCase
    from app.infrastructure.services.ai_provider import OpenAIAnalysisProvider

    use_case = AICodeReviewUseCase(
        ai_provider=OpenAIAnalysisProvider(),
        submission_repo=repo,
    )
    review = await use_case.execute(submission_id)
    return ResponseWrapper.ok(data={"review": review})


@router.get("/{submission_id}/report")
async def report(submission_id: str) -> ResponseWrapper:
    """Возвращает ссылку на PDF-отчёт по проверке."""
    return ResponseWrapper.ok(
        data={"report_url": f"/api/v1/reports/submissions/{submission_id}.pdf"}
    )
