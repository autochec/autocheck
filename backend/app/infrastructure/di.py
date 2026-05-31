"""
DI-фабрика — собирает зависимости для use cases и передаёт в эндпоинты.
Dependency Inversion: все зависимости инжектируются через этот модуль.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from functools import lru_cache
from typing import Annotated

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.infrastructure.db.base import get_db
from app.infrastructure.repositories.user_repository import UserRepository
from app.infrastructure.repositories.assignment_repository import AssignmentRepository
from app.infrastructure.repositories.submission_repository import (
    CheckResultRepository, SubmissionRepository,
)
from app.infrastructure.checkers.static_analysis import StaticAnalysisChecker
from app.infrastructure.checkers.architecture_checker import ArchitectureChecker
from app.infrastructure.checkers.build_checker import BuildChecker
from app.infrastructure.checkers.test_checker import TestChecker
from app.infrastructure.checkers.documentation_checker import DocumentationChecker
from app.infrastructure.checkers.git_practices_checker import GitPracticesChecker
from app.infrastructure.checkers.check_orchestrator import CheckOrchestrator
from app.infrastructure.checkers.result_calculator import ResultCalculator
from app.infrastructure.services.ai_provider import OpenAIAnalysisProvider
from app.application.use_cases.submit_assignment import SubmitAssignmentUseCase
from app.application.use_cases.run_checks import RunChecksUseCase

logger = logging.getLogger(__name__)


# ── Репозитории ────────────────────────────────────────────────────────────────

def get_user_repo(session: AsyncSession = Depends(get_db)) -> UserRepository:
    """FastAPI-зависимость: UserRepository для текущей сессии."""
    return UserRepository(session)


def get_assignment_repo(session: AsyncSession = Depends(get_db)) -> AssignmentRepository:
    """FastAPI-зависимость: AssignmentRepository для текущей сессии."""
    return AssignmentRepository(session)


def get_submission_repo(session: AsyncSession = Depends(get_db)) -> SubmissionRepository:
    """FastAPI-зависимость: SubmissionRepository для текущей сессии."""
    return SubmissionRepository(session)


def get_result_repo(session: AsyncSession = Depends(get_db)) -> CheckResultRepository:
    """FastAPI-зависимость: CheckResultRepository для текущей сессии."""
    return CheckResultRepository(session)


# ── Чекеры (синглтоны, не зависят от сессии) ─────────────────────────────────

def get_checkers() -> list:
    """
    Возвращает список всех активных чекеров.
    Для добавления нового чекера — просто добавить экземпляр в список.
    CheckOrchestrator не меняется (Open/Closed).
    """
    return [
        StaticAnalysisChecker(),
        ArchitectureChecker(),
        BuildChecker(),
        TestChecker(),
        DocumentationChecker(),
        GitPracticesChecker(),
    ]


def get_orchestrator() -> CheckOrchestrator:
    """FastAPI-зависимость: CheckOrchestrator с полным набором чекеров."""
    return CheckOrchestrator(checkers=get_checkers())


# ── Use Cases ─────────────────────────────────────────────────────────────────

def get_submit_use_case(
    submission_repo: SubmissionRepository = Depends(get_submission_repo),
) -> SubmitAssignmentUseCase:
    """FastAPI-зависимость: SubmitAssignmentUseCase."""
    return SubmitAssignmentUseCase(submission_repo=submission_repo)


def get_run_checks_use_case(
    submission_repo: SubmissionRepository = Depends(get_submission_repo),
    result_repo: CheckResultRepository = Depends(get_result_repo),
) -> RunChecksUseCase:
    """FastAPI-зависимость: RunChecksUseCase с полным набором чекеров."""
    return RunChecksUseCase(
        checkers=get_checkers(),
        submission_repo=submission_repo,
        result_repo=result_repo,
    )


# ── AI-провайдер ──────────────────────────────────────────────────────────────

def get_ai_provider() -> OpenAIAnalysisProvider:
    """FastAPI-зависимость: IAnalysisProvider (OpenAI-совместимый)."""
    return OpenAIAnalysisProvider()


def get_ai_review_use_case(
    submission_repo: SubmissionRepository = Depends(get_submission_repo),
) -> "AICodeReviewUseCase":
    """FastAPI-зависимость: AICodeReviewUseCase с реальным AI-провайдером."""
    from app.application.use_cases.ai_review import AICodeReviewUseCase
    return AICodeReviewUseCase(
        ai_provider=OpenAIAnalysisProvider(),
        submission_repo=submission_repo,
    )
