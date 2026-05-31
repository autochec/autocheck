"""
Настройка логирования для всего приложения.
Формат сообщений: [Компонент]: Событие — Детали

Уровни:
    DEBUG  — техническая информация для разработки
    INFO   — ключевые события жизненного цикла
    ERROR  — исключительные ситуации и сбои

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import sys


def setup_logging() -> None:
    """Инициализирует формат и уровни логирования для всего приложения."""
    fmt = "%(asctime)s | %(levelname)-8s | %(name)s | %(message)s"
    logging.basicConfig(
        level=logging.DEBUG,
        format=fmt,
        handlers=[logging.StreamHandler(sys.stdout)],
    )
    # Приглушаем шумные библиотеки
    logging.getLogger("uvicorn.access").setLevel(logging.WARNING)
    logging.getLogger("sqlalchemy.engine").setLevel(logging.WARNING)
