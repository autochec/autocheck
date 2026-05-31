"""
GitPracticesChecker — анализирует Git-историю репозитория.
Проверяет: осмысленность коммитов, feature-ветки, наличие PR.
Работает только если source_type == "git".

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
import re
import subprocess
import uuid

from app.domain.interfaces.checkers import IChecker
from app.domain.models.submission import CheckResult, CheckStatus, Submission

logger = logging.getLogger(__name__)
TIMEOUT_SECONDS = 60

# Бессодержательные сообщения коммитов
BAD_COMMIT_PATTERN = re.compile(
    r"^(fix|update|wip|minor|temp|test|commit|changes?|stuff|asdf|\.+)$",
    re.IGNORECASE,
)


class GitPracticesChecker(IChecker):
    """
    Анализирует качество Git-практик в репозитории кандидата.
    Доступен только для решений, переданных через Git URL.
    """

    async def run(self, submission: Submission) -> CheckResult:
        """
        Клонирует репозиторий (если git) и анализирует историю.

        Args:
            submission: Объект решения; source_type="git" и source_path=git_url.
        Returns:
            CheckResult с баллом 0–100.
        """
        logger.info(f"[GitPracticesChecker]: Запуск — submissionId={submission.id}")

        if submission.source_type != "git":
            return CheckResult(
                id=str(uuid.uuid4()),
                submission_id=submission.id,
                checker="git_practices",
                status=CheckStatus.PASSED,
                score=50.0,
                message="Решение загружено как ZIP — Git-анализ пропущен",
                details="",
            )

        repo_dir = f"/tmp/git_check_{submission.id}"
        try:
            # Клонируем репозиторий
            clone = subprocess.run(
                ["git", "clone", "--depth=50", submission.source_path, repo_dir],
                capture_output=True, text=True, timeout=TIMEOUT_SECONDS,
            )
            if clone.returncode != 0:
                logger.error(
                    f"[GitPracticesChecker]: Ошибка клонирования — {clone.stderr[:200]}"
                )
                return self._error(submission.id, "Не удалось клонировать репозиторий")

            score, details = self._analyze(repo_dir)
            status = CheckStatus.PASSED if score >= 50 else CheckStatus.FAILED
            logger.debug(f"[GitPracticesChecker]: Готово — score={score}")
            return CheckResult(
                id=str(uuid.uuid4()),
                submission_id=submission.id,
                checker="git_practices",
                status=status,
                score=score,
                message=f"Балл за Git-практики: {score}/100",
                details=details,
            )
        except subprocess.TimeoutExpired:
            logger.error("[GitPracticesChecker]: Ошибка — Превышено время выполнения")
            return self._error(submission.id, "Превышено время выполнения")
        except Exception as e:
            logger.error(f"[GitPracticesChecker]: Ошибка — {e}")
            return self._error(submission.id, str(e))
        finally:
            # Чистим временную директорию
            subprocess.run(["rm", "-rf", repo_dir], check=False)

    def _analyze(self, repo_dir: str) -> tuple[float, str]:
        """
        Анализирует Git-историю: коммиты, ветки, PR.

        Args:
            repo_dir: Директория склонированного репозитория.
        Returns:
            Кортеж (score, details_text).
        """
        score = 0.0
        details = []

        # 1. Осмысленные сообщения коммитов (до 40 баллов)
        log = subprocess.run(
            ["git", "log", "--format=%s", "-n", "30"],
            cwd=repo_dir, capture_output=True, text=True,
        )
        messages = [m.strip() for m in log.stdout.splitlines() if m.strip()]
        if messages:
            bad = sum(1 for m in messages if BAD_COMMIT_PATTERN.match(m))
            good_ratio = 1 - (bad / len(messages))
            commit_score = round(good_ratio * 40, 1)
            score += commit_score
            details.append(f"Коммитов: {len(messages)}, плохих: {bad} (+{commit_score})")

        # 2. Feature-ветки (до 30 баллов)
        branches = subprocess.run(
            ["git", "branch", "-a"],
            cwd=repo_dir, capture_output=True, text=True,
        )
        branch_names = [b.strip().lstrip("* ") for b in branches.stdout.splitlines()]
        non_main = [b for b in branch_names if not re.match(r"(main|master|HEAD)", b)]
        if non_main:
            score += min(30.0, len(non_main) * 10.0)
            details.append(f"Feature-веток: {len(non_main)} (+{min(30, len(non_main)*10)})")
        else:
            details.append("Feature-веток нет (+0)")

        # 3. Несколько контрибьюторов / PR (до 30 баллов) — упрощённая проверка
        authors = subprocess.run(
            ["git", "log", "--format=%ae", "-n", "30"],
            cwd=repo_dir, capture_output=True, text=True,
        )
        unique_authors = len(set(authors.stdout.splitlines()))
        if unique_authors > 1:
            score += 30.0
            details.append(f"Контрибьюторов: {unique_authors} (+30)")
        else:
            score += 15.0
            details.append("Один контрибьютор (+15)")

        return min(100.0, score), "\n".join(details)

    @staticmethod
    def _error(submission_id: str, message: str) -> CheckResult:
        return CheckResult(
            id=str(uuid.uuid4()),
            submission_id=submission_id,
            checker="git_practices",
            status=CheckStatus.ERROR,
            score=0.0,
            message=message,
            details="",
        )
