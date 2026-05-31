"""
OpenAIAnalysisProvider — реализует IAnalysisProvider через OpenAI-совместимый API.
Graceful degradation: если AI_API_KEY не задан или запрос упал — возвращает
"AI-анализ недоступен", система продолжает работу без AI.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging

import httpx

from app.core.config import settings
from app.domain.interfaces.checkers import IAnalysisProvider

logger = logging.getLogger(__name__)

AI_UNAVAILABLE = "AI-анализ недоступен"


class OpenAIAnalysisProvider(IAnalysisProvider):
    """
    Отправляет запрос к LLM API в формате OpenAI /v1/chat/completions.
    API-ключ и базовый URL берутся из переменных окружения AI_API_KEY / AI_API_URL.
    Совместим с: OpenAI, Azure OpenAI, Ollama, LocalAI, LM Studio.
    """

    async def analyze(self, prompt: str) -> str:
        """
        Вызывает LLM API и возвращает структурированный анализ.

        Args:
            prompt: Промпт с системным сообщением, описанием задания и кодом.
        Returns:
            Текст анализа (три секции: хорошо / улучшить / замечания)
            или "AI-анализ недоступен" при ошибке.
        """
        if not settings.AI_API_KEY:
            logger.error(
                "[OpenAIAnalysisProvider]: Ошибка — AI_API_KEY не задан, "
                "graceful degradation"
            )
            return AI_UNAVAILABLE

        logger.info(
            f"[OpenAIAnalysisProvider]: Запрос к LLM — model={settings.AI_MODEL}"
        )
        try:
            async with httpx.AsyncClient(timeout=90) as client:
                response = await client.post(
                    f"{settings.AI_API_URL}/chat/completions",
                    headers={
                        "Authorization": f"Bearer {settings.AI_API_KEY}",
                        "Content-Type": "application/json",
                    },
                    json={
                        "model": settings.AI_MODEL,
                        "messages": [{"role": "user", "content": prompt}],
                        "max_tokens": 1500,
                        "temperature": 0.3,   # детерминированность для ревью
                    },
                )
                response.raise_for_status()
                data = response.json()
                text: str = data["choices"][0]["message"]["content"]
                logger.debug(
                    f"[OpenAIAnalysisProvider]: Ответ получен — "
                    f"tokens={data.get('usage', {}).get('total_tokens', '?')}"
                )
                return text
        except httpx.TimeoutException:
            logger.error("[OpenAIAnalysisProvider]: Ошибка — таймаут запроса к LLM")
            return AI_UNAVAILABLE
        except httpx.HTTPStatusError as e:
            logger.error(
                f"[OpenAIAnalysisProvider]: Ошибка — HTTP {e.response.status_code}"
            )
            return AI_UNAVAILABLE
        except Exception as e:
            logger.error(f"[OpenAIAnalysisProvider]: Ошибка — {e}")
            return AI_UNAVAILABLE
