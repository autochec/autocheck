"""
AICodeReviewUseCase — формирует промпт из кода кандидата и вызывает LLM.
Промпт включает: описание задания + фрагменты ≤10 файлов, ≤200 строк каждый.
Результат сохраняется в БД.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
from pathlib import Path

from app.domain.interfaces.checkers import IAnalysisProvider
from app.domain.interfaces.repositories import ISubmissionRepository

logger = logging.getLogger(__name__)

MAX_FILES = 10
MAX_LINES = 200

# Расширения файлов, которые отправляются модели
CODE_EXTENSIONS = {".py", ".kt", ".swift", ".dart", ".ts", ".tsx", ".js"}

# Системный промпт — структура ответа обязательна
SYSTEM_PROMPT = """Ты — опытный мобильный разработчик, выполняющий код-ревью тестового задания.
Ответь строго в следующем формате (три секции, каждая с минимум 3 пунктами):

## ✅ Что реализовано хорошо
- пункт 1
- пункт 2
- пункт 3

## 🔧 Что требует улучшения
- пункт 1
- пункт 2
- пункт 3

## 📌 Конкретные замечания по коду
- пункт 1
- пункт 2
- пункт 3

Будь конкретен: указывай имена файлов, классов, методов где возможно."""


class AICodeReviewUseCase:
    """
    Формирует промпт из решения кандидата и получает AI-анализ.

    Зависимости через конструктор (Dependency Inversion):
        ai_provider:      IAnalysisProvider — заменяемый LLM-провайдер.
        submission_repo:  ISubmissionRepository — для сохранения результата.
    """

    def __init__(
        self,
        ai_provider: IAnalysisProvider,
        submission_repo: ISubmissionRepository,
    ) -> None:
        self._ai = ai_provider
        self._repo = submission_repo

    async def execute(self, submission_id: str, assignment_description: str = "") -> str:
        """
        Запускает AI-анализ кода для указанного решения.

        Args:
            submission_id:           ID решения.
            assignment_description:  Описание задания (требования к архитектуре и стеку).
        Returns:
            Текст AI-анализа или "AI-анализ недоступен".
        """
        logger.info(
            f"[AICodeReviewUseCase]: Запуск — submissionId={submission_id}"
        )
        submission = await self._repo.get_by_id(submission_id)
        if not submission:
            logger.error(
                f"[AICodeReviewUseCase]: Ошибка — решение не найдено: {submission_id}"
            )
            return "AI-анализ недоступен"

        # Если уже есть кэшированный результат — возвращаем его
        if submission.ai_review:
            logger.debug(
                f"[AICodeReviewUseCase]: Возврат кэша — submissionId={submission_id}"
            )
            return submission.ai_review

        try:
            code_fragments = self._collect_code(submission.source_path)
            prompt = self._build_prompt(assignment_description, code_fragments)
            review = await self._ai.analyze(prompt)

            # Сохраняем результат в БД
            await self._repo.save_ai_review(submission_id, review)
            logger.info(
                f"[AICodeReviewUseCase]: Анализ сохранён — submissionId={submission_id}"
            )
            return review
        except Exception as e:
            logger.error(f"[AICodeReviewUseCase]: Ошибка — {e}")
            return "AI-анализ недоступен"

    def _collect_code(self, source_dir: str) -> list[dict]:
        """
        Собирает фрагменты кода из директории.
        Берёт не более MAX_FILES файлов, каждый обрезается до MAX_LINES строк.

        Args:
            source_dir: Директория с исходным кодом кандидата.
        Returns:
            Список словарей {"path": str, "content": str}.
        """
        fragments = []
        if not os.path.isdir(source_dir):
            return fragments

        for path in sorted(Path(source_dir).rglob("*")):
            if len(fragments) >= MAX_FILES:
                break
            if path.suffix not in CODE_EXTENSIONS:
                continue
            # Пропускаем build-артефакты и зависимости
            if any(part in path.parts for part in ("build", "node_modules", ".gradle", "Pods")):
                continue
            try:
                lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
                content = "\n".join(lines[:MAX_LINES])
                if content.strip():
                    fragments.append({
                        "path": str(path.relative_to(source_dir)),
                        "content": content,
                    })
            except OSError:
                pass

        return fragments

    @staticmethod
    def _build_prompt(description: str, fragments: list[dict]) -> str:
        """
        Собирает итоговый промпт для LLM.

        Args:
            description: Описание тестового задания.
            fragments:   Список фрагментов кода.
        Returns:
            Строка промпта для отправки в LLM.
        """
        parts = [SYSTEM_PROMPT, ""]

        if description:
            parts.append(f"## Описание задания\n{description}\n")

        parts.append("## Код кандидата\n")
        for frag in fragments:
            parts.append(f"### {frag['path']}\n```\n{frag['content']}\n```\n")

        if not fragments:
            parts.append("_(исходный код недоступен для анализа)_\n")

        return "\n".join(parts)
