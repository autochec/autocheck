"""
AutoCheckMobile — точка входа FastAPI-приложения.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.core.errors import http_exception_handler, unhandled_exception_handler
from app.core.logging_setup import setup_logging

setup_logging()
logger = logging.getLogger(__name__)

app = FastAPI(
    title="AutoCheckMobile API",
    version="1.0.0",
    docs_url="/api/docs",
    redoc_url="/api/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.add_exception_handler(HTTPException, http_exception_handler)
app.add_exception_handler(Exception, unhandled_exception_handler)

# Подключаем роутер v1
from app.api.v1 import router as v1_router  # noqa: E402
app.include_router(v1_router, prefix="/api/v1")


@app.on_event("startup")
async def startup() -> None:
    logger.info("[App]: Запуск AutoCheckMobile")


@app.on_event("shutdown")
async def shutdown() -> None:
    logger.info("[App]: Остановка AutoCheckMobile")


@app.get("/health")
async def health():
    """Health-check эндпоинт для docker-compose и балансировщика."""
    return {"status": "ok"}
