"""
ResponseWrapper — единый формат всех ответов API: {data, error, meta}.
DRY: все контроллеры используют этот класс, не создают свои форматы.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from typing import Any, Optional
from pydantic import BaseModel


class ResponseWrapper(BaseModel):
    """
    Стандартная обёртка ответа API.

    Поля:
        data  — полезная нагрузка (при успехе).
        error — текст ошибки (при сбое).
        meta  — метаданные (пагинация, версия и т.д.).
    """
    data: Optional[Any] = None
    error: Optional[str] = None
    meta: Optional[dict] = None

    @classmethod
    def ok(cls, data: Any, meta: dict | None = None) -> "ResponseWrapper":
        """
        Формирует успешный ответ.

        Args:
            data: Возвращаемые данные.
            meta: Опциональные метаданные.
        Returns:
            ResponseWrapper с заполненным полем data.
        """
        return cls(data=data, meta=meta)

    @classmethod
    def fail(cls, message: str) -> "ResponseWrapper":
        """
        Формирует ответ с ошибкой.

        Args:
            message: Текст ошибки для клиента.
        Returns:
            ResponseWrapper с заполненным полем error.
        """
        return cls(error=message)
