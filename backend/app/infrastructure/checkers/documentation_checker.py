"""
DocumentationChecker — анализирует документацию исходного кода.
Балл = % задокументированных публичных классов/методов.
Дополнительно проверяет наличие README.md.

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

# Паттерны публичных объявлений и документационных комментариев
PATTERNS = {
    "python":  (re.compile(r"^(class|def)\s+[A-Z_a-z]"), re.compile(r'"""')),
    "kotlin":  (re.compile(r"^(class|fun|interface)\s+[A-Z_a-z]"), re.compile(r"/\*\*")),
    "swift":   (re.compile(r"^(class|func|struct|protocol)\s+[A-Z_a-z]"), re.compile(r"///")),
    "dart":    (re.compile(r"^(class|void|Future)\s+[A-Z_a-z]"), re.compile(r"///")),
}
EXT_MAP = {".py": "python", ".kt": "kotlin", ".swift": "swift", ".dart": "dart"}


class DocumentationChecker(IChecker):
    """
    Проверяет наличие документации в публичных классах и методах.
    Анализирует .py, .kt, .swift, .dart файлы.
    """

    async def run(self, submission: Submission) -> CheckResult:
        """
        Анализирует исходный код на наличие документации.

        Args:
            submission: Объект решения с путём к исходному коду.
        Returns:
            CheckResult с баллом 0–100 (% задокументированных сущностей).
        """
        logger.info(
            f"[DocumentationChecker]: Запуск — submissionId={submission.id}"
        )
        source_dir = submission.source_path

        if not os.path.isdir(source_dir):
            return self._error(submission.id, "Директория с кодом не найдена")

        has_readme = os.path.exists(os.path.join(source_dir, "README.md"))
        total, documented = self._analyze(source_dir)

        # README бонус: если нет публичных сущностей, но README есть — минимум 20 баллов
        if total == 0:
            score = 20.0 if has_readme else 0.0
        else:
            score = round((documented / total) * 100, 2)
            if has_readme:
                score = min(100.0, score + 5.0)  # бонус за README

        status = CheckStatus.PASSED if score >= 50 else CheckStatus.FAILED
        logger.debug(
            f"[DocumentationChecker]: Готово — "
            f"documented={documented}/{total}, readme={has_readme}, score={score}"
        )
        return CheckResult(
            id=str(uuid.uuid4()),
            submission_id=submission.id,
            checker="documentation",
            status=status,
            score=score,
            message=(
                f"Задокументировано: {documented}/{total} сущностей, "
                f"README: {'есть' if has_readme else 'отсутствует'}"
            ),
            details=f"Сущностей: {total}, задокументированных: {documented}",
        )

    def _analyze(self, source_dir: str) -> tuple[int, int]:
        """
        Подсчитывает публичные объявления и проверяет наличие докстрингов.

        Args:
            source_dir: Корневая директория с исходным кодом.
        Returns:
            Кортеж (total, documented).
        """
        total = documented = 0
        for root, _, files in os.walk(source_dir):
            for filename in files:
                ext = os.path.splitext(filename)[1]
                lang = EXT_MAP.get(ext)
                if not lang:
                    continue
                filepath = os.path.join(root, filename)
                t, d = self._analyze_file(filepath, *PATTERNS[lang])
                total += t
                documented += d
        return total, documented

    @staticmethod
    def _analyze_file(
        filepath: str,
        decl_pattern: re.Pattern,
        doc_pattern: re.Pattern,
    ) -> tuple[int, int]:
        """
        Анализирует один файл: считает объявления и документацию рядом с ними.

        Args:
            filepath:     Путь к файлу.
            decl_pattern: Regex для поиска объявления класса/функции.
            doc_pattern:  Regex для поиска документационного комментария.
        Returns:
            Кортеж (объявлений, задокументированных).
        """
        try:
            with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
                lines = f.readlines()
        except OSError:
            return 0, 0

        total = documented = 0
        for i, line in enumerate(lines):
            stripped = line.strip()
            if decl_pattern.search(stripped):
                total += 1
                # Проверяем строки вокруг объявления (±3) на наличие докстринга
                context_start = max(0, i - 1)
                context_end = min(len(lines), i + 4)
                context = "".join(lines[context_start:context_end])
                if doc_pattern.search(context):
                    documented += 1
        return total, documented

    @staticmethod
    def _error(submission_id: str, message: str) -> CheckResult:
        return CheckResult(
            id=str(uuid.uuid4()),
            submission_id=submission_id,
            checker="documentation",
            status=CheckStatus.ERROR,
            score=0.0,
            message=message,
            details="",
        )
