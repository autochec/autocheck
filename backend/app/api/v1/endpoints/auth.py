"""
AuthController — аутентификация и профиль.
POST /auth/login | /auth/register | /auth/logout
GET  /auth/profile

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel, EmailStr
from app.core.response import ResponseWrapper
from app.core.security import create_access_token, hash_password, get_current_user_id

logger = logging.getLogger(__name__)
router = APIRouter()


class LoginRequest(BaseModel):
    email: str
    password: str


class RegisterRequest(BaseModel):
    email: str
    full_name: str
    password: str
    role: str = "candidate"


@router.post("/login")
async def login(body: LoginRequest) -> ResponseWrapper:
    """Вход по email + пароль, возвращает JWT Bearer-токен."""
    logger.info(f"[AuthController]: Вход — email={body.email}")
    try:
        # TODO Sprint-2: подключить UserRepository + verify_password
        token = create_access_token(subject="placeholder_user_id")
        logger.debug(f"[AuthController]: Токен выдан — email={body.email}")
        return ResponseWrapper.ok(data={"access_token": token, "token_type": "bearer"})
    except Exception as e:
        logger.error(f"[AuthController]: Ошибка входа — {e}")
        raise HTTPException(status_code=401, detail="Неверный email или пароль")


@router.post("/register", status_code=status.HTTP_201_CREATED)
async def register(body: RegisterRequest) -> ResponseWrapper:
    """Регистрация нового пользователя."""
    logger.info(f"[AuthController]: Регистрация — email={body.email}")
    try:
        # TODO Sprint-2: сохранить через UserRepository
        hashed = hash_password(body.password)
        return ResponseWrapper.ok(data={"message": "Пользователь зарегистрирован"})
    except Exception as e:
        logger.error(f"[AuthController]: Ошибка регистрации — {e}")
        raise HTTPException(status_code=409, detail="Email уже зарегистрирован")


@router.post("/logout")
async def logout(user_id: str = Depends(get_current_user_id)) -> ResponseWrapper:
    """Завершение текущей сессии."""
    logger.info(f"[AuthController]: Выход — userId={user_id}")
    return ResponseWrapper.ok(data={"message": "Сессия завершена"})


@router.get("/profile")
async def profile(user_id: str = Depends(get_current_user_id)) -> ResponseWrapper:
    """Профиль текущего пользователя по JWT."""
    logger.debug(f"[AuthController]: Профиль — userId={user_id}")
    return ResponseWrapper.ok(data={"id": user_id, "role": "candidate"})
