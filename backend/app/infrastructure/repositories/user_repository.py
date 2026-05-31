"""
UserRepository — SQLAlchemy-реализация IUserRepository.
Single Responsibility: только доступ к таблице users.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from typing import Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.domain.interfaces.repositories import IUserRepository
from app.domain.models.user import User, UserRole
from app.infrastructure.db.models import UserORM

logger = logging.getLogger(__name__)


class UserRepository(IUserRepository):
    """
    Реализация IUserRepository через SQLAlchemy AsyncSession.

    Публичные методы:
        get_by_id(user_id) -> Optional[User]
        get_by_email(email) -> Optional[User]
        create(user) -> User
    """

    def __init__(self, session: AsyncSession) -> None:
        self._session = session

    async def get_by_id(self, user_id: str) -> Optional[User]:
        """
        Находит пользователя по UUID.

        Args:
            user_id: UUID пользователя.
        Returns:
            User или None если не найден.
        """
        logger.debug(f"[UserRepository]: get_by_id — userId={user_id}")
        try:
            result = await self._session.execute(
                select(UserORM).where(UserORM.id == user_id)
            )
            orm = result.scalar_one_or_none()
            return self._to_domain(orm) if orm else None
        except Exception as e:
            logger.error(f"[UserRepository]: Ошибка get_by_id — {e}")
            raise

    async def get_by_email(self, email: str) -> Optional[User]:
        """
        Находит пользователя по email.

        Args:
            email: Email пользователя.
        Returns:
            User или None если не найден.
        """
        logger.debug(f"[UserRepository]: get_by_email — email={email}")
        try:
            result = await self._session.execute(
                select(UserORM).where(UserORM.email == email)
            )
            orm = result.scalar_one_or_none()
            return self._to_domain(orm) if orm else None
        except Exception as e:
            logger.error(f"[UserRepository]: Ошибка get_by_email — {e}")
            raise

    async def create(self, user: User) -> User:
        """
        Сохраняет нового пользователя в БД.

        Args:
            user: Доменный объект User.
        Returns:
            Сохранённый User.
        """
        logger.info(f"[UserRepository]: create — email={user.email}, role={user.role}")
        try:
            orm = UserORM(
                id=user.id,
                email=user.email,
                full_name=user.full_name,
                role=user.role.value,
                hashed_password=user.hashed_password,
            )
            self._session.add(orm)
            await self._session.flush()
            logger.debug(f"[UserRepository]: Пользователь создан — id={user.id}")
            return user
        except Exception as e:
            logger.error(f"[UserRepository]: Ошибка create — {e}")
            raise

    @staticmethod
    def _to_domain(orm: UserORM) -> User:
        """Конвертирует ORM-объект в доменную модель."""
        return User(
            id=orm.id,
            email=orm.email,
            full_name=orm.full_name,
            role=UserRole(orm.role),
            hashed_password=orm.hashed_password,
        )
