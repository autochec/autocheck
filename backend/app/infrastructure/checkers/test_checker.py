"""
TestChecker — запускает unit-тесты в Docker, парсит JUnit XML.
Балл = (passed / total) × 100. Таймаут 3 минуты.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
import subprocess
import uuid
import xml.etree.ElementTree as ET
from glob import glob

from app.domain.interfaces.checkers import IChecker
from app.domain.models.submission import CheckResult, CheckStatus, Submission
from app.infrastructure.checkers.docker_runner import DockerRunner, DOCKER_IMAGES

logger = logging.getLogger(__name__)


class TestChecker(IChecker):
    """
    Запускает тесты проекта и анализирует JUnit XML-отчёт.
    Код исполняется в изолированном Docker-контейнере.
    """

    def __init__(self) -> None:
        self._runner = DockerRunner()

    async def run(self, submission: Submission) -> CheckResult:
        """
        Запускает тесты и считает балл по JUnit XML.

        Args:
            submission: Объект решения с source_path.
        Returns:
            CheckResult с баллом 0–100.
        """
        logger.info(f"[TestChecker]: Запуск — submissionId={submission.id}")
        source_dir = submission.source_path

        if not os.path.isdir(source_dir):
            return self._error(submission.id, "Директория с кодом не найдена")

        test_cmd, image = self._detect_test(source_dir)
        if not test_cmd:
            return CheckResult(
                id=str(uuid.uuid4()), submission_id=submission.id,
                checker="test", status=CheckStatus.FAILED, score=0.0,
                message="Тесты не обнаружены в проекте", details="",
            )

        try:
            self._runner.run(command=test_cmd, source_dir=source_dir, image=image)
            passed, total = self._parse_junit(source_dir)
            score = round((passed / total) * 100, 2) if total > 0 else 0.0
            status = CheckStatus.PASSED if score >= 50 else CheckStatus.FAILED

            logger.debug(
                f"[TestChecker]: Готово — passed={passed}/{total}, score={score}"
            )
            return CheckResult(
                id=str(uuid.uuid4()), submission_id=submission.id,
                checker="test", status=status, score=score,
                message=f"Тестов: {passed}/{total} прошли",
                details=f"passed={passed}, total={total}",
            )
        except subprocess.TimeoutExpired:
            logger.error("[TestChecker]: Ошибка — Превышено время выполнения")
            return self._error(submission.id, "Превышено время выполнения")
        except Exception as e:
            logger.error(f"[TestChecker]: Ошибка — {e}")
            return self._error(submission.id, str(e))

    @staticmethod
    def _detect_test(source_dir: str) -> tuple[list | None, str]:
        files = os.listdir(source_dir)
        if "pubspec.yaml" in files:
            return ["flutter", "test", "--reporter=json"], DOCKER_IMAGES["flutter"]
        if "build.gradle" in files or "build.gradle.kts" in files:
            cmd = "./gradlew" if os.path.exists(os.path.join(source_dir, "gradlew")) else "gradle"
            return [cmd, "test"], DOCKER_IMAGES["android"]
        if "package.json" in files:
            return ["npm", "test", "--", "--watchAll=false", "--ci"], DOCKER_IMAGES["node"]
        return None, DOCKER_IMAGES["default"]

    @staticmethod
    def _parse_junit(source_dir: str) -> tuple[int, int]:
        patterns = [
            "**/build/test-results/**/*.xml",
            "**/test-results/**/*.xml",
        ]
        xml_files = []
        for p in patterns:
            xml_files.extend(glob(os.path.join(source_dir, p), recursive=True))

        passed = total = 0
        for xf in xml_files:
            try:
                tree = ET.parse(xf)
                for suite in tree.getroot().iter("testsuite"):
                    t = int(suite.get("tests", 0))
                    f = int(suite.get("failures", 0))
                    e = int(suite.get("errors", 0))
                    total += t
                    passed += t - f - e
            except (ET.ParseError, ValueError):
                pass
        return passed, total

    @staticmethod
    def _error(submission_id: str, message: str) -> CheckResult:
        return CheckResult(
            id=str(uuid.uuid4()), submission_id=submission_id,
            checker="test", status=CheckStatus.ERROR,
            score=0.0, message=message, details="",
        )
