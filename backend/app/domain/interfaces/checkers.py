"""
IChecker и IAnalysisProvider — контракты движка проверки.

Open/Closed: новый чекер = новый класс, реализующий IChecker.
CheckOrchestrator и RunChecksUseCase не меняются.

Dependency Inversion: CheckOrchestrator зависит от IChecker[],
AICodeReviewer — от IAnalysisProvider, не от конкретных реализаций.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from abc import ABC, abstractmethod

from app.domain.models.submission import Submission, CheckResult


class IChecker(ABC):
    """
    Базовый интерфейс для всех видов автоматической проверки.
    Liskov: любая реализация (LintChecker, BuildChecker, …) взаимозаменяема.
    """

    @abstractmethod
    async def run(self, submission: Submission) -> CheckResult:
        """
        Выполняет проверку решения кандидата.

        Args:
            submission: Объект решения с исходным кодом.
        Returns:
            CheckResult со статусом, баллом (0–100), сообщением и деталями.
        """
        ...


class IAnalysisProvider(ABC):
    """
    Абстракция AI-провайдера.
    Позволяет менять LLM (OpenAI → Ollama → другой) без изменения бизнес-кода.
    """

    @abstractmethod
    async def analyze(self, prompt: str) -> str:
        """
        Отправляет промпт к LLM и возвращает текстовый ответ.

        Args:
            prompt: Сформированный промпт с описанием задания и фрагментами кода.
        Returns:
            Структурированный текстовый анализ или "AI-анализ недоступен".
        """
        ...
