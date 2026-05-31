"""
SubmissionRepository — SQLAlchemy-реализация ISubmissionRepository и ICheckResultRepository.
Single Responsibility: доступ к таблицам submissions и check_results.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import uuid
from typing import List, Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.domain.interfaces.repositories import ISubmissionRepository, ICheckResultRepository
from app.domain.models.submission import (
    CheckResult, CheckStatus, Submission, SubmissionStatus,
)
from app.infrastructure.db.models import CheckResultORM, SubmissionORM

logger = logging.getLogger(__name__)


class SubmissionRepository(ISubmissionRepository):
    """
    Реализация ISubmissionRepository через SQLAlchemy AsyncSession.

    Публичные методы:
        get_all() -> List[Submission]
        get_by_id(id) -> Optional[Submission]
        create(submission) -> Submission
        update_status(id, status) -> None
        update_verdict(id, verdict) -> None
        save_ai_review(id, review) -> None
    """

    def __init__(self, session: AsyncSession) -> None:
        self._session = session

    async def get_all(self) -> List[Submission]:
        """Возвращает все решения (без results для экономии)."""
        logger.debug("[SubmissionRepository]: get_all")
        try:
            result = await self._session.execute(select(SubmissionORM))
            return [self._to_domain(o) for o in result.scalars().all()]
        except Exception as e:
            logger.error(f"[SubmissionRepository]: Ошибка get_all — {e}")
            raise

    async def get_by_id(self, submission_id: str) -> Optional[Submission]:
        """
        Находит решение по ID вместе с результатами чекеров.

        Args:
            submission_id: UUID решения.
        Returns:
            Submission с заполненным results[] или None.
        """
        logger.debug(f"[SubmissionRepository]: get_by_id — id={submission_id}")
        try:
            result = await self._session.execute(
                select(SubmissionORM)
                .options(selectinload(SubmissionORM.results))
                .where(SubmissionORM.id == submission_id)
            )
            orm = result.scalar_one_or_none()
            return self._to_domain_with_results(orm) if orm else None
        except Exception as e:
            logger.error(f"[SubmissionRepository]: Ошибка get_by_id — {e}")
            raise

    async def create(self, submission: Submission) -> Submission:
        """
        Сохраняет новое решение со статусом PENDING.

        Args:
            submission: Доменный объект Submission.
        Returns:
            Сохранённый Submission.
        """
        logger.info(
            f"[SubmissionRepository]: create — "
            f"assignmentId={submission.assignment_id}, candidateId={submission.candidate_id}"
        )
        try:
            orm = SubmissionORM(
                id=submission.id,
                assignment_id=submission.assignment_id,
                candidate_id=submission.candidate_id,
                source_type=submission.source_type,
                source_path=submission.source_path,
                status=submission.status.value,
            )
            self._session.add(orm)
            await self._session.flush()
            logger.debug(f"[SubmissionRepository]: Создано — id={submission.id}")
            return submission
        except Exception as e:
            logger.error(f"[SubmissionRepository]: Ошибка create — {e}")
            raise

    async def update_status(self, submission_id: str, status: str) -> None:
        """
        Обновляет статус проверки (pending → running → done | error).

        Args:
            submission_id: UUID решения.
            status:        Новый статус (строка или SubmissionStatus).
        """
        logger.info(
            f"[SubmissionRepository]: update_status — "
            f"id={submission_id}, status={status}"
        )
        try:
            result = await self._session.execute(
                select(SubmissionORM).where(SubmissionORM.id == submission_id)
            )
            orm = result.scalar_one_or_none()
            if orm:
                # Принимаем и enum, и строку
                orm.status = status.value if hasattr(status, "value") else status
                await self._session.flush()
        except Exception as e:
            logger.error(f"[SubmissionRepository]: Ошибка update_status — {e}")
            raise

    async def update_verdict(self, submission_id: str, verdict: str) -> None:
        """
        Сохраняет вердикт Эксперта (accepted | rejected).

        Args:
            submission_id: UUID решения.
            verdict:       "accepted" или "rejected".
        """
        logger.info(
            f"[SubmissionRepository]: update_verdict — "
            f"id={submission_id}, verdict={verdict}"
        )
        try:
            result = await self._session.execute(
                select(SubmissionORM).where(SubmissionORM.id == submission_id)
            )
            orm = result.scalar_one_or_none()
            if orm:
                orm.verdict = verdict
                await self._session.flush()
        except Exception as e:
            logger.error(f"[SubmissionRepository]: Ошибка update_verdict — {e}")
            raise

    async def save_ai_review(self, submission_id: str, review: str) -> None:
        """
        Сохраняет текст AI-анализа.

        Args:
            submission_id: UUID решения.
            review:        Текст от LLM.
        """
        logger.debug(f"[SubmissionRepository]: save_ai_review — id={submission_id}")
        try:
            result = await self._session.execute(
                select(SubmissionORM).where(SubmissionORM.id == submission_id)
            )
            orm = result.scalar_one_or_none()
            if orm:
                orm.ai_review = review
                await self._session.flush()
        except Exception as e:
            logger.error(f"[SubmissionRepository]: Ошибка save_ai_review — {e}")
            raise

    # ── Конвертеры ────────────────────────────────────────────────────────────

    @staticmethod
    def _to_domain(orm: SubmissionORM) -> Submission:
        """Конвертирует ORM-объект в доменную модель (без results)."""
        return Submission(
            id=orm.id,
            assignment_id=orm.assignment_id,
            candidate_id=orm.candidate_id,
            source_type=orm.source_type,
            source_path=orm.source_path,
            status=SubmissionStatus(orm.status),
            total_score=orm.total_score,
            verdict=orm.verdict,
            ai_review=orm.ai_review,
        )

    @staticmethod
    def _to_domain_with_results(orm: SubmissionORM) -> Submission:
        """Конвертирует ORM-объект вместе с CheckResult[]."""
        results = [
            CheckResult(
                id=r.id,
                submission_id=r.submission_id,
                checker=r.checker,
                status=CheckStatus(r.status),
                score=r.score,
                message=r.message,
                details=r.details,
            )
            for r in (orm.results or [])
        ]
        return Submission(
            id=orm.id,
            assignment_id=orm.assignment_id,
            candidate_id=orm.candidate_id,
            source_type=orm.source_type,
            source_path=orm.source_path,
            status=SubmissionStatus(orm.status),
            total_score=orm.total_score,
            verdict=orm.verdict,
            ai_review=orm.ai_review,
            results=results,
        )


class CheckResultRepository(ICheckResultRepository):
    """
    Реализация ICheckResultRepository через SQLAlchemy AsyncSession.

    Публичные методы:
        get_by_submission(submission_id) -> List[CheckResult]
        create_many(results) -> None
    """

    def __init__(self, session: AsyncSession) -> None:
        self._session = session

    async def get_by_submission(self, submission_id: str) -> List[CheckResult]:
        """
        Возвращает все результаты чекеров для одного решения.

        Args:
            submission_id: UUID решения.
        Returns:
            Список CheckResult.
        """
        logger.debug(
            f"[CheckResultRepository]: get_by_submission — id={submission_id}"
        )
        try:
            result = await self._session.execute(
                select(CheckResultORM).where(
                    CheckResultORM.submission_id == submission_id
                )
            )
            return [
                CheckResult(
                    id=r.id,
                    submission_id=r.submission_id,
                    checker=r.checker,
                    status=CheckStatus(r.status),
                    score=r.score,
                    message=r.message,
                    details=r.details,
                )
                for r in result.scalars().all()
            ]
        except Exception as e:
            logger.error(f"[CheckResultRepository]: Ошибка get_by_submission — {e}")
            raise

    async def create_many(self, results: List[CheckResult]) -> None:
        """
        Пакетно сохраняет результаты чекеров.

        Args:
            results: Список CheckResult для сохранения.
        """
        logger.info(
            f"[CheckResultRepository]: create_many — count={len(results)}"
        )
        try:
            orm_objects = [
                CheckResultORM(
                    id=r.id,
                    submission_id=r.submission_id,
                    checker=r.checker,
                    status=r.status.value,
                    score=r.score,
                    message=r.message,
                    details=r.details,
                )
                for r in results
            ]
            self._session.add_all(orm_objects)
            await self._session.flush()
            logger.debug(
                f"[CheckResultRepository]: Сохранено {len(orm_objects)} результатов"
            )
        except Exception as e:
            logger.error(f"[CheckResultRepository]: Ошибка create_many — {e}")
            raise
