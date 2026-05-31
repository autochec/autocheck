"""
CheckOrchestrator — запускает IChecker[] и собирает CheckResult[].
Open/Closed: добавление нового чекера не изменяет этот класс.
Dependency Inversion: зависит от IChecker[], не от конкретных реализаций.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import asyncio
import logging
from typing import List

from app.domain.interfaces.checkers import IChecker
from app.domain.models.submission import CheckResult, Submission

logger = logging.getLogger(__name__)


class CheckOrchestrator:
    """
    Оркестрирует параллельный запуск всех активных чекеров.
    Graceful failure: если один чекер упал, остальные продолжают работу.

    Зависимости через конструктор (Dependency Inversion):
        checkers: IChecker[] — список реализаций интерфейса.
    """

    def __init__(self, checkers: List[IChecker]) -> None:
        self._checkers = checkers

    async def run_all(self, submission: Submission) -> List[CheckResult]:
        """
        Запускает все чекеры параллельно через asyncio.gather.
        Ошибка одного чекера не прерывает остальные.

        Args:
            submission: Объект решения для проверки.
        Returns:
            Список CheckResult от всех завершившихся чекеров.
        """
        logger.info(
            f"[CheckOrchestrator]: Запуск {len(self._checkers)} чекеров — "
            f"submissionId={submission.id}"
        )
        tasks = [self._run_safe(checker, submission) for checker in self._checkers]
        results = await asyncio.gather(*tasks)
        completed = [r for r in results if r is not None]
        logger.info(
            f"[CheckOrchestrator]: Завершено {len(completed)}/{len(self._checkers)} — "
            f"submissionId={submission.id}"
        )
        return completed

    async def _run_safe(
        self, checker: IChecker, submission: Submission
    ) -> CheckResult | None:
        """
        Запускает один чекер с перехватом исключений.

        Args:
            checker:    Реализация IChecker.
            submission: Объект решения.
        Returns:
            CheckResult или None при критической ошибке чекера.
        """
        try:
            result = await checker.run(submission)
            logger.debug(
                f"[CheckOrchestrator]: Чекер {checker.__class__.__name__} — "
                f"score={result.score}, status={result.status}"
            )
            return result
        except Exception as e:
            logger.error(
                f"[CheckOrchestrator]: Ошибка чекера "
                f"{checker.__class__.__name__} — {e}"
            )
            return None
