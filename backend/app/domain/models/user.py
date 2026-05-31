"""
User — доменная модель пользователя системы.
Чистая бизнес-сущность без зависимостей от ORM или фреймворка.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from dataclasses import dataclass
from enum import Enum


class UserRole(str, Enum):
    EXPERT = "expert"
    CANDIDATE = "candidate"


@dataclass
class User:
    """
    Бизнес-сущность пользователя.

    Поля:
        id:              UUID пользователя.
        email:           Уникальный email.
        full_name:       Полное имя.
        role:            Роль в системе (expert | candidate).
        hashed_password: Bcrypt-хэш пароля.
    """
    id: str
    email: str
    full_name: str
    role: UserRole
    hashed_password: str
