"""
AssignmentRepository — SQLAlchemy-реализация IAssignmentRepository.
Single Responsibility: только доступ к таблице assignments.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from typing import List, Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.domain.interfaces.repositories import IAssignmentRepository
from app.domain.models.assignment import Assignment, CheckerConfig
from app.infrastructure.db.models import AssignmentORM

logger = logging.getLogger(__name__)


class AssignmentRepository(IAssignmentRepository):
    """
    Реализация IAssignmentRepository через SQLAlchemy AsyncSession.

    Публичные методы:
        get_all() -> List[Assignment]
        get_by_id(id) -> Optional[Assignment]
        create(assignment) -> Assignment
        update(assignment) -> Assignment
        delete(id) -> None
    """

    def __init__(self, session: AsyncSession) -> None:
        self._session = session

    async def get_all(self) -> List[Assignment]:
        """Возвращает все тестовые задания."""
        logger.debug("[AssignmentRepository]: get_all")
        try:
            result = await self._session.execute(select(AssignmentORM))
            return [self._to_domain(o) for o in result.scalars().all()]
        except Exception as e:
            logger.error(f"[AssignmentRepository]: Ошибка get_all — {e}")
            raise

    async def get_by_id(self, assignment_id: str) -> Optional[Assignment]:
        """
        Находит задание по UUID.

        Args:
            assignment_id: UUID задания.
        Returns:
            Assignment или None.
        """
        logger.debug(f"[AssignmentRepository]: get_by_id — id={assignment_id}")
        try:
            result = await self._session.execute(
                select(AssignmentORM).where(AssignmentORM.id == assignment_id)
            )
            orm = result.scalar_one_or_none()
            return self._to_domain(orm) if orm else None
        except Exception as e:
            logger.error(f"[AssignmentRepository]: Ошибка get_by_id — {e}")
            raise

    async def create(self, assignment: Assignment) -> Assignment:
        """
        Создаёт новое тестовое задание.

        Args:
            assignment: Доменный объект Assignment.
        Returns:
            Сохранённый Assignment.
        """
        logger.info(
            f"[AssignmentRepository]: create — "
            f"title={assignment.title}, expertId={assignment.expert_id}"
        )
        try:
            # Сериализуем CheckerConfig в JSON-совместимый словарь
            configs_json = {
                k: {"enabled": v.enabled, "weight": v.weight}
                for k, v in assignment.checker_configs.items()
            }
            orm = AssignmentORM(
                id=assignment.id,
                title=assignment.title,
                description=assignment.description,
                expert_id=assignment.expert_id,
                checker_configs=configs_json,
            )
            self._session.add(orm)
            await self._session.flush()
            logger.debug(f"[AssignmentRepository]: Создано — id={assignment.id}")
            return assignment
        except Exception as e:
            logger.error(f"[AssignmentRepository]: Ошибка create — {e}")
            raise

    async def update(self, assignment: Assignment) -> Assignment:
        """
        Обновляет данные задания.

        Args:
            assignment: Обновлённый объект Assignment.
        Returns:
            Обновлённый Assignment.
        """
        logger.info(f"[AssignmentRepository]: update — id={assignment.id}")
        try:
            result = await self._session.execute(
                select(AssignmentORM).where(AssignmentORM.id == assignment.id)
            )
            orm = result.scalar_one_or_none()
            if orm:
                orm.title = assignment.title
                orm.description = assignment.description
                orm.checker_configs = {
                    k: {"enabled": v.enabled, "weight": v.weight}
                    for k, v in assignment.checker_configs.items()
                }
                await self._session.flush()
            return assignment
        except Exception as e:
            logger.error(f"[AssignmentRepository]: Ошибка update — {e}")
            raise

    async def delete(self, assignment_id: str) -> None:
        """
        Удаляет задание по UUID.

        Args:
            assignment_id: UUID задания для удаления.
        """
        logger.info(f"[AssignmentRepository]: delete — id={assignment_id}")
        try:
            result = await self._session.execute(
                select(AssignmentORM).where(AssignmentORM.id == assignment_id)
            )
            orm = result.scalar_one_or_none()
            if orm:
                await self._session.delete(orm)
                await self._session.flush()
        except Exception as e:
            logger.error(f"[AssignmentRepository]: Ошибка delete — {e}")
            raise

    @staticmethod
    def _to_domain(orm: AssignmentORM) -> Assignment:
        """Конвертирует ORM-объект в доменную модель."""
        configs = {
            k: CheckerConfig(enabled=v.get("enabled", True), weight=v.get("weight", 0.0))
            for k, v in (orm.checker_configs or {}).items()
        }
        return Assignment(
            id=orm.id,
            title=orm.title,
            description=orm.description,
            expert_id=orm.expert_id,
            checker_configs=configs,
        )
