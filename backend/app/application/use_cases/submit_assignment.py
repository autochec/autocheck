"""
SubmitAssignmentUseCase — бизнес-сценарий загрузки решения кандидатом.
Сохраняет Submission и ставит задачу в очередь Celery.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import uuid

from app.domain.interfaces.repositories import ISubmissionRepository
from app.domain.models.submission import Submission, SubmissionStatus

logger = logging.getLogger(__name__)


class SubmitAssignmentUseCase:
    """
    Принимает решение кандидата и инициирует асинхронную проверку.

    Зависимости через конструктор (Dependency Inversion):
        submission_repo: ISubmissionRepository — любая реализация БД.
    """

    def __init__(self, submission_repo: ISubmissionRepository) -> None:
        self._repo = submission_repo

    async def execute(
        self,
        assignment_id: str,
        candidate_id: str,
        source_type: str,
        source_path: str,
    ) -> Submission:
        """
        Создаёт Submission и ставит задачу проверки в очередь.

        Args:
            assignment_id: ID тестового задания.
            candidate_id:  ID кандидата-отправителя.
            source_type:   "zip" или "git".
            source_path:   Путь к ZIP-файлу или git URL.
        Returns:
            Созданный Submission со статусом PENDING.
        """
        logger.info(
            f"[SubmitAssignmentUseCase]: Загрузка решения — "
            f"assignment={assignment_id}, candidate={candidate_id}"
        )
        try:
            submission = Submission(
                id=str(uuid.uuid4()),
                assignment_id=assignment_id,
                candidate_id=candidate_id,
                source_type=source_type,
                source_path=source_path,
                status=SubmissionStatus.PENDING,
            )
            saved = await self._repo.create(submission)

            # Ставим задачу в Celery-очередь
            from worker.tasks import run_checks_task
            run_checks_task.delay(saved.id)

            logger.info(
                f"[SubmitAssignmentUseCase]: Задача в очереди — submissionId={saved.id}"
            )
            return saved
        except Exception as e:
            logger.error(f"[SubmitAssignmentUseCase]: Ошибка — {e}")
            raise
