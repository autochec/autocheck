"""
Celery-задачи для асинхронной проверки решений.
Персистентность: task_acks_late=True — задача не теряется при падении воркера.
Статус: pending → running → done | error.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import asyncio
import logging

from celery import Celery

from app.core.config import settings

logger = logging.getLogger(__name__)

celery_app = Celery(
    "autocheckmobile_worker",
    broker=settings.REDIS_URL,
    backend=settings.REDIS_URL,
)
celery_app.conf.update(
    task_serializer="json",
    result_serializer="json",
    accept_content=["json"],
    task_acks_late=True,               # подтверждение только после успеха
    task_reject_on_worker_lost=True,   # переставить в очередь при падении воркера
    worker_prefetch_multiplier=1,      # один чекер за раз (тяжёлые задачи)
)


@celery_app.task(name="run_checks_task", bind=True, max_retries=3)
def run_checks_task(self, submission_id: str) -> None:
    """
    Запускает полную проверку одного решения.
    При ошибке — повторная попытка через 30 секунд, максимум 3 раза.

    Args:
        submission_id: ID Submission для проверки.
    """
    logger.info(f"[Worker]: Задача получена — submissionId={submission_id}")
    try:
        asyncio.run(_execute_checks(submission_id))
        logger.info(f"[Worker]: Задача завершена — submissionId={submission_id}")
    except Exception as e:
        logger.error(f"[Worker]: Ошибка — submissionId={submission_id}, {e}")
        raise self.retry(exc=e, countdown=30)


async def _execute_checks(submission_id: str) -> None:
    """
    Async-обёртка: инициализирует зависимости и запускает RunChecksUseCase.

    Args:
        submission_id: ID решения.
    """
    from app.infrastructure.db.base import AsyncSessionLocal
    from app.infrastructure.repositories.submission_repository import (
        SubmissionRepository, CheckResultRepository,
    )
    from app.infrastructure.checkers.check_orchestrator import CheckOrchestrator
    from app.infrastructure.checkers.result_calculator import ResultCalculator
    from app.infrastructure.di import get_checkers
    from app.application.use_cases.run_checks import RunChecksUseCase

    logger.debug(
        f"[Worker]: Инициализация зависимостей — submissionId={submission_id}"
    )
    async with AsyncSessionLocal() as session:
        submission_repo = SubmissionRepository(session)
        result_repo = CheckResultRepository(session)
        checkers = get_checkers()

        use_case = RunChecksUseCase(
            checkers=checkers,
            submission_repo=submission_repo,
            result_repo=result_repo,
        )
        await use_case.execute(submission_id)
        await session.commit()
