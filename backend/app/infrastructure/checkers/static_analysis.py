"""
StaticAnalysisChecker — запускает lint в изолированном Docker-контейнере.
Балл = max(0, 100 - errors*5 - warnings*1). Таймаут 3 минуты.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
import subprocess
import uuid

from app.domain.interfaces.checkers import IChecker
from app.domain.models.submission import CheckResult, CheckStatus, Submission
from app.infrastructure.checkers.docker_runner import DockerRunner

logger = logging.getLogger(__name__)
TIMEOUT_SECONDS = 180


class StaticAnalysisChecker(IChecker):
    """
    Статический анализ кода кандидата через lint-инструменты.
    Sprint-3: каждый чекер запускается в изолированном Docker-контейнере.
    """

    def __init__(self) -> None:
        self._runner = DockerRunner()

    async def run(self, submission: Submission) -> CheckResult:
        """
        Запускает lint в Docker, парсит вывод, возвращает балл.

        Args:
            submission: Объект решения с source_path к директории.
        Returns:
            CheckResult с баллом 0–100.
        """
        logger.info(f"[StaticAnalysisChecker]: Запуск — submissionId={submission.id}")
        source_dir = submission.source_path

        if not os.path.isdir(source_dir):
            return self._error(submission.id, "Директория с кодом не найдена")

        lint_cmd, image = self._detect_lint(source_dir)
        logger.debug(
            f"[StaticAnalysisChecker]: Команда — {lint_cmd}, образ={image}"
        )
        try:
            returncode, stdout, stderr = self._runner.run(
                command=lint_cmd,
                source_dir=source_dir,
                image=image,
            )
            output = stdout + stderr
            errors, warnings = self._parse_output(output)
            score = max(0.0, 100.0 - errors * 5 - warnings * 1)
            status = CheckStatus.PASSED if score >= 50 else CheckStatus.FAILED

            logger.debug(
                f"[StaticAnalysisChecker]: Готово — "
                f"errors={errors}, warnings={warnings}, score={score}"
            )
            return CheckResult(
                id=str(uuid.uuid4()),
                submission_id=submission.id,
                checker="static_analysis",
                status=status,
                score=score,
                message=f"Ошибок: {errors}, предупреждений: {warnings}",
                details=output[:4000],
            )
        except subprocess.TimeoutExpired:
            logger.error(
                "[StaticAnalysisChecker]: Ошибка — Превышено время выполнения"
            )
            return self._error(submission.id, "Превышено время выполнения")
        except Exception as e:
            logger.error(
                f"[StaticAnalysisChecker]: Ошибка — Не удалось запустить lint ({e})"
            )
            return self._error(submission.id, str(e))

    @staticmethod
    def _detect_lint(source_dir: str) -> tuple[list, str]:
        """
        Определяет lint-команду и Docker-образ по типу проекта.

        Args:
            source_dir: Директория с исходным кодом.
        Returns:
            Кортеж (команда_как_список, docker_image).
        """
        files = os.listdir(source_dir)
        if "pubspec.yaml" in files:
            return ["flutter", "analyze", "--no-fatal-infos"], "ghcr.io/cirruslabs/flutter:stable"
        if "build.gradle" in files or "build.gradle.kts" in files:
            cmd = "./gradlew" if os.path.exists(
                os.path.join(source_dir, "gradlew")) else "gradle"
            return [cmd, "lint", "--continue"], "mingc/android-build-box:latest"
        if "package.json" in files:
            return ["npx", "eslint", ".", "--format=compact"], "node:20-alpine"
        return ["find", ".", "-name", "*.py", "-exec", "python", "-m", "py_compile", "{}", ";"], "python:3.12-slim"

    @staticmethod
    def _parse_output(output: str) -> tuple[int, int]:
        """
        Парсит текстовый вывод линтера.

        Args:
            output: Объединённый stdout + stderr.
        Returns:
            Кортеж (количество_ошибок, количество_предупреждений).
        """
        lower = output.lower()
        errors = lower.count("error")
        warnings = lower.count("warning")
        return errors, warnings

    @staticmethod
    def _error(submission_id: str, message: str) -> CheckResult:
        return CheckResult(
            id=str(uuid.uuid4()),
            submission_id=submission_id,
            checker="static_analysis",
            status=CheckStatus.ERROR,
            score=0.0,
            message=message,
            details="",
        )
