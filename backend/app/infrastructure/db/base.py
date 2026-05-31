"""
Настройка SQLAlchemy async-движка и фабрики сессий.
Единый источник подключения к БД для всего приложения.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase

from app.core.config import settings

logger = logging.getLogger(__name__)

# Async-движок PostgreSQL
engine = create_async_engine(
    settings.DATABASE_URL,
    echo=settings.DEBUG,   # SQL-запросы в лог только в DEBUG-режиме
    pool_pre_ping=True,    # проверяет соединение перед использованием из пула
)

# Фабрика сессий
AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)


class Base(DeclarativeBase):
    """Базовый класс для всех ORM-моделей."""
    pass


async def get_db() -> AsyncSession:
    """
    FastAPI-зависимость: создаёт сессию на время запроса и закрывает после.

    Yields:
        AsyncSession — активная сессия SQLAlchemy.
    """
    logger.debug("[DB]: Открытие сессии")
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
            logger.debug("[DB]: Сессия закрыта (commit)")
        except Exception as e:
            await session.rollback()
            logger.error(f"[DB]: Ошибка — rollback: {e}")
            raise
