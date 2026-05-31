"""
RunChecksUseCase — оркестрация запуска всех чекеров для одного решения.
Open/Closed: новый IChecker добавляется без изменения этого файла.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from typing import List

from app.domain.interfaces.checkers import IChecker
from app.domain.interfaces.repositories import ISubmissionRepository, ICheckResultRepository
from app.domain.models.submission import SubmissionStatus

logger = logging.getLogger(__name__)


class RunChecksUseCase:
    """
    Запускает все активные чекеры, собирает результаты,
    обновляет статус Submission поэтапно.

    Зависимости через конструктор (Dependency Inversion):
        checkers:         IChecker[] — список реализаций.
        submission_repo:  ISubmissionRepository.
        result_repo:      ICheckResultRepository.
    """

    def __init__(
        self,
        checkers: List[IChecker],
        submission_repo: ISubmissionRepository,
        result_repo: ICheckResultRepository,
    ) -> None:
        self._checkers = checkers
        self._submission_repo = submission_repo
        self._result_repo = result_repo

    async def execute(self, submission_id: str) -> None:
        """
        Запускает проверку по ID решения.
        Статус: pending → running → done | error.

        Args:
            submission_id: ID Submission для проверки.
        """
        logger.info(f"[RunChecksUseCase]: Проверка запущена — submissionId={submission_id}")
        await self._submission_repo.update_status(submission_id, SubmissionStatus.RUNNING)
        try:
            submission = await self._submission_repo.get_by_id(submission_id)
            results = []

            for checker in self._checkers:
                # Graceful failure: один чекер упал — остальные продолжают
                try:
                    result = await checker.run(submission)
                    results.append(result)
                    logger.debug(
                        f"[RunChecksUseCase]: Чекер завершён — "
                        f"checker={checker.__class__.__name__}, score={result.score}"
                    )
                except Exception as e:
                    logger.error(
                        f"[RunChecksUseCase]: Ошибка чекера "
                        f"{checker.__class__.__name__} — {e}"
                    )

            await self._result_repo.create_many(results)
            await self._submission_repo.update_status(submission_id, SubmissionStatus.DONE)
            logger.info(
                f"[RunChecksUseCase]: Проверка завершена — submissionId={submission_id}"
            )
        except Exception as e:
            logger.error(
                f"[RunChecksUseCase]: Критическая ошибка — "
                f"submissionId={submission_id}, {e}"
            )
            await self._submission_repo.update_status(submission_id, SubmissionStatus.ERROR)
            raise
