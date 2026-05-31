"""
ErrorHandler — централизованная обработка HTTP-ошибок.
DRY: ошибки не дублируются в каждом контроллере.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from fastapi import Request, HTTPException
from fastapi.responses import JSONResponse

from app.core.response import ResponseWrapper

logger = logging.getLogger(__name__)


async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """
    Перехватывает FastAPI HTTPException и возвращает ResponseWrapper.

    Args:
        request: Входящий запрос.
        exc:     HTTPException с кодом и деталями.
    Returns:
        JSONResponse в формате ResponseWrapper.
    """
    logger.error(f"[ErrorHandler]: HTTP {exc.status_code} — {exc.detail} "
                 f"path={request.url.path}")
    return JSONResponse(
        status_code=exc.status_code,
        content=ResponseWrapper.fail(str(exc.detail)).model_dump(),
    )


async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """
    Перехватывает все необработанные исключения (500).

    Args:
        request: Входящий запрос.
        exc:     Любое Python-исключение.
    Returns:
        JSONResponse 500 в формате ResponseWrapper.
    """
    logger.error(f"[ErrorHandler]: Необработанная ошибка — {type(exc).__name__}: {exc} "
                 f"path={request.url.path}")
    return JSONResponse(
        status_code=500,
        content=ResponseWrapper.fail("Внутренняя ошибка сервера").model_dump(),
    )
