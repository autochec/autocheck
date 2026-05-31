"""
ResultCalculator — взвешенный итоговый балл по всем чекерам.
Формула: sum(score_i * weight_i) / sum(weight_i), нормированный к 100.
Веса берутся из конфигурации задания (Assignment.checker_configs).

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from typing import List, Dict

from app.domain.models.submission import CheckResult

logger = logging.getLogger(__name__)


class ResultCalculator:
    """
    Рассчитывает итоговый балл решения на основе взвешенных результатов чекеров.
    """

    def calculate(
        self,
        results: List[CheckResult],
        weights: Dict[str, float],
    ) -> float:
        """
        Взвешенная сумма баллов, нормированная к 100.

        Args:
            results: Список CheckResult от отработавших чекеров.
            weights: {checker_name: weight} из CheckerConfig задания.
        Returns:
            Итоговый балл 0–100, округлённый до 2 знаков.
        """
        total_weight = 0.0
        weighted_sum = 0.0

        for result in results:
            w = weights.get(result.checker, 0.0)
            # Чекеры без веса в конфиге не участвуют в расчёте
            if w <= 0:
                continue
            weighted_sum += result.score * w
            total_weight += w

        if total_weight == 0:
            logger.error(
                "[ResultCalculator]: Ошибка — нет активных чекеров с ненулевыми весами"
            )
            return 0.0

        final = round(weighted_sum / total_weight, 2)
        logger.debug(f"[ResultCalculator]: Итоговый балл — score={final}")
        return final
