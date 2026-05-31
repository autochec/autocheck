"""
Утилиты безопасности: хэширование паролей, создание и декодирование JWT.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from datetime import datetime, timedelta
from typing import Optional

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from passlib.context import CryptContext

from app.core.config import settings

logger = logging.getLogger(__name__)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
bearer_scheme = HTTPBearer()


def hash_password(password: str) -> str:
    """Хэширует пароль bcrypt."""
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    """Проверяет пароль против bcrypt-хэша."""
    return pwd_context.verify(plain, hashed)


def create_access_token(subject: str) -> str:
    """
    Создаёт подписанный JWT Bearer-токен.

    Args:
        subject: ID пользователя (строка).
    Returns:
        Подписанный JWT-токен.
    """
    expire = datetime.utcnow() + timedelta(minutes=settings.JWT_EXPIRE_MINUTES)
    payload = {"sub": subject, "exp": expire}
    token = jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)
    logger.debug(f"[Security]: Токен создан — sub={subject}")
    return token


def decode_token(token: str) -> Optional[str]:
    """
    Декодирует JWT, возвращает subject или None при ошибке.

    Args:
        token: Bearer-токен из заголовка Authorization.
    Returns:
        subject (user_id) или None.
    """
    try:
        payload = jwt.decode(
            token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM]
        )
        return payload.get("sub")
    except jwt.ExpiredSignatureError:
        logger.error("[Security]: Ошибка — токен истёк")
        return None
    except jwt.InvalidTokenError as e:
        logger.error(f"[Security]: Ошибка — невалидный токен: {e}")
        return None


def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme),
) -> str:
    """
    FastAPI-зависимость: извлекает user_id из Bearer-токена.

    Returns:
        user_id строкой.
    Raises:
        HTTPException 401: токен отсутствует или невалиден.
    """
    user_id = decode_token(credentials.credentials)
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Недействительный или истёкший токен",
        )
    return user_id
