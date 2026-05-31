"""
BuildChecker — собирает проект кандидата в изолированном Docker-контейнере.
Балл: 100 при успехе, 0 при неудаче. Таймаут 3 минуты.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
import subprocess
import uuid

from app.domain.interfaces.checkers import IChecker
from app.domain.models.submission import CheckResult, CheckStatus, Submission
from app.infrastructure.checkers.docker_runner import DockerRunner, DOCKER_IMAGES

logger = logging.getLogger(__name__)


class BuildChecker(IChecker):
    """
    Сборка проекта кандидата.
    Код исполняется в изолированном Docker-контейнере (--network none).
    """

    def __init__(self) -> None:
        self._runner = DockerRunner()

    async def run(self, submission: Submission) -> CheckResult:
        """
        Определяет тип проекта и запускает сборку в Docker.

        Args:
            submission: Объект решения с source_path.
        Returns:
            CheckResult: score=100 при успехе, score=0 при неудаче.
        """
        logger.info(f"[BuildChecker]: Запуск — submissionId={submission.id}")
        source_dir = submission.source_path

        if not os.path.isdir(source_dir):
            return self._error(submission.id, "Директория с кодом не найдена")

        build_cmd, image, project_type = self._detect_build(source_dir)
        if not build_cmd:
            return self._error(submission.id, "Неизвестный тип проекта")

        logger.debug(
            f"[BuildChecker]: Тип проекта={project_type}, образ={image}"
        )
        try:
            returncode, stdout, stderr = self._runner.run(
                command=build_cmd,
                source_dir=source_dir,
                image=image,
            )
            success = returncode == 0
            output = stdout + stderr
            error_lines = [l for l in output.splitlines() if "error" in l.lower()]
            score = 100.0 if success else 0.0
            status = CheckStatus.PASSED if success else CheckStatus.FAILED

            logger.debug(
                f"[BuildChecker]: Готово — success={success}, "
                f"errors={len(error_lines)}"
            )
            return CheckResult(
                id=str(uuid.uuid4()),
                submission_id=submission.id,
                checker="build",
                status=status,
                score=score,
                message="Сборка успешна" if success else f"Ошибок компиляции: {len(error_lines)}",
                details=output[:4000],
            )
        except subprocess.TimeoutExpired:
            logger.error("[BuildChecker]: Ошибка — Превышено время выполнения")
            return self._error(submission.id, "Превышено время выполнения")
        except Exception as e:
            logger.error(f"[BuildChecker]: Ошибка — {e}")
            return self._error(submission.id, str(e))

    @staticmethod
    def _detect_build(source_dir: str) -> tuple[list | None, str, str | None]:
        """
        Определяет команду сборки, образ и тип проекта.

        Args:
            source_dir: Директория с исходным кодом.
        Returns:
            Кортеж (команда, docker_image, тип_проекта).
        """
        files = os.listdir(source_dir)
        if "pubspec.yaml" in files:
            return ["flutter", "build", "apk", "--debug"], DOCKER_IMAGES["flutter"], "Flutter"
        if "build.gradle" in files or "build.gradle.kts" in files:
            cmd = "./gradlew" if os.path.exists(os.path.join(source_dir, "gradlew")) else "gradle"
            return [cmd, "assembleDebug"], DOCKER_IMAGES["android"], "Android"
        if "package.json" in files:
            return ["npm", "run", "build"], DOCKER_IMAGES["node"], "React Native"
        return None, DOCKER_IMAGES["default"], None

    @staticmethod
    def _error(submission_id: str, message: str) -> CheckResult:
        return CheckResult(
            id=str(uuid.uuid4()), submission_id=submission_id,
            checker="build", status=CheckStatus.ERROR,
            score=0.0, message=message, details="",
        )
