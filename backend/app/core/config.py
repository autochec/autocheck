"""
ConfigService — единый источник конфигурации приложения.
Все чувствительные данные берутся из переменных окружения, не захардкожены.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """
    Центральный конфиг-объект.
    Значения читаются из .env или переменных окружения.
    """
    # База данных
    DATABASE_URL: str

    # Redis / Celery
    REDIS_URL: str = "redis://redis:6379/0"

    # JWT — обязательно задать случайное значение в .env
    JWT_SECRET: str
    JWT_ALGORITHM: str = "HS256"
    JWT_EXPIRE_MINUTES: int = 60 * 24  # 24 часа

    # AI-интеграция (опционально — без ключа включается graceful degradation)
    AI_API_KEY: str = ""
    AI_API_URL: str = "https://api.openai.com/v1"
    AI_MODEL: str = "gpt-4o-mini"

    # Приложение
    APP_URL: str = "http://localhost:8000"
    ALLOWED_ORIGINS: List[str] = ["http://localhost:3000"]
    DEBUG: bool = False

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
