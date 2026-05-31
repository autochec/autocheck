"""
ArchitectureChecker — анализирует структуру каталогов решения.
+25 баллов за каждый обнаруженный слой (domain/data/presentation).
-20 за каждое нарушение: domain импортирует из data/infrastructure.
Таймаут 3 минуты.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
import re
import uuid

from app.domain.interfaces.checkers import IChecker
from app.domain.models.submission import CheckResult, CheckStatus, Submission

logger = logging.getLogger(__name__)
TIMEOUT_SECONDS = 180

# Ожидаемые слои — хотя бы одно вхождение в структуре папок
LAYER_PATTERNS = [
    re.compile(r"(domain|entities|models)", re.IGNORECASE),
    re.compile(r"(data|repository|repositories|infrastructure)", re.IGNORECASE),
    re.compile(r"(presentation|ui|screens|view|viewmodel|bloc)", re.IGNORECASE),
]

# Паттерн нарушения: domain-файл импортирует data/infrastructure
VIOLATION_PATTERN = re.compile(
    r"import\s+.*?(data|infrastructure|repository)", re.IGNORECASE
)


class ArchitectureChecker(IChecker):
    """
    Статический анализ архитектурного разделения на слои.
    Запускается в изолированном Docker-контейнере (Sprint-3).
    """

    async def run(self, submission: Submission) -> CheckResult:
        """
        Проверяет архитектуру решения по структуре каталогов.

        Args:
            submission: Объект решения с путём к исходному коду.
        Returns:
            CheckResult с баллом 0–100.
        """
        logger.info(f"[ArchitectureChecker]: Запуск — submissionId={submission.id}")
        try:
            source_dir = submission.source_path
            if not os.path.isdir(source_dir):
                return self._error_result(submission.id, "Директория с кодом не найдена")

            layers_found, violations = self._analyze(source_dir)
            score = max(0.0, layers_found * 25.0 - violations * 20.0)
            score = min(score, 100.0)
            status = CheckStatus.PASSED if score >= 50 else CheckStatus.FAILED

            logger.debug(
                f"[ArchitectureChecker]: Готово — "
                f"layers={layers_found}, violations={violations}, score={score}"
            )
            return CheckResult(
                id=str(uuid.uuid4()),
                submission_id=submission.id,
                checker="architecture",
                status=status,
                score=score,
                message=f"Слоёв найдено: {layers_found}/3, нарушений: {violations}",
                details=f"Слоёв: {layers_found}, нарушений DIP: {violations}",
            )
        except Exception as e:
            logger.error(f"[ArchitectureChecker]: Ошибка — {e}")
            return self._error_result(submission.id, str(e))

    def _analyze(self, source_dir: str) -> tuple[int, int]:
        """
        Обходит структуру каталогов и ищет слои и нарушения.

        Args:
            source_dir: Корневая директория с исходным кодом.
        Returns:
            Кортеж (количество_найденных_слоёв, количество_нарушений).
        """
        # Проверяем наличие слоёв по именам директорий
        all_dirs = []
        domain_files = []

        for root, dirs, files in os.walk(source_dir):
            all_dirs.extend(dirs)
            # Собираем файлы из domain-директорий для проверки нарушений
            if re.search(r"(domain|entities)", root, re.IGNORECASE):
                for f in files:
                    if f.endswith((".py", ".kt", ".swift", ".dart")):
                        domain_files.append(os.path.join(root, f))

        layers_found = sum(
            1 for pattern in LAYER_PATTERNS
            if any(pattern.search(d) for d in all_dirs)
        )

        # Ищем нарушения: domain импортирует data/infrastructure
        violations = 0
        for filepath in domain_files:
            try:
                with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read()
                violations += len(VIOLATION_PATTERN.findall(content))
            except OSError:
                pass

        return layers_found, violations

    @staticmethod
    def _error_result(submission_id: str, message: str) -> CheckResult:
        return CheckResult(
            id=str(uuid.uuid4()),
            submission_id=submission_id,
            checker="architecture",
            status=CheckStatus.ERROR,
            score=0.0,
            message=message,
            details="",
        )
