"""
Alembic env.py — конфигурация миграций для async SQLAlchemy.
Импортирует все ORM-модели, чтобы autogenerate видел таблицы.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import asyncio
from logging.config import fileConfig

from sqlalchemy import pool
from sqlalchemy.ext.asyncio import async_engine_from_config

from alembic import context

# Импортируем Base и все ORM-модели для autogenerate
from app.infrastructure.db.base import Base
from app.infrastructure.db import models  # noqa: F401 — нужен для регистрации таблиц
from app.core.config import settings

config = context.config

# Подставляем DATABASE_URL из .env (не хардкодим в alembic.ini)
config.set_main_option("sqlalchemy.url", settings.DATABASE_URL)

if config.config_file_name is not None:
    fileConfig(config.config_file_name)

target_metadata = Base.metadata


def run_migrations_offline() -> None:
    """Offline-режим: генерирует SQL без подключения к БД."""
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )
    with context.begin_transaction():
        context.run_migrations()


def do_run_migrations(connection):
    context.configure(connection=connection, target_metadata=target_metadata)
    with context.begin_transaction():
        context.run_migrations()


async def run_migrations_online() -> None:
    """Online-режим: подключается к БД и применяет миграции."""
    connectable = async_engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )
    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)
    await connectable.dispose()


if context.is_offline_mode():
    run_migrations_offline()
else:
    asyncio.run(run_migrations_online())
