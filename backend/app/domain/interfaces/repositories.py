"""
Repository Interfaces — абстракции доступа к данным (Dependency Inversion).
Конкретные реализации живут в infrastructure/repositories/.
Код приложения зависит только от этих интерфейсов.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from abc import ABC, abstractmethod
from typing import List, Optional

from app.domain.models.user import User
from app.domain.models.assignment import Assignment
from app.domain.models.submission import Submission, CheckResult


class IUserRepository(ABC):
    """Контракт доступа к пользователям."""

    @abstractmethod
    async def get_by_id(self, user_id: str) -> Optional[User]: ...

    @abstractmethod
    async def get_by_email(self, email: str) -> Optional[User]: ...

    @abstractmethod
    async def create(self, user: User) -> User: ...


class IAssignmentRepository(ABC):
    """Контракт CRUD-доступа к тестовым заданиям."""

    @abstractmethod
    async def get_all(self) -> List[Assignment]: ...

    @abstractmethod
    async def get_by_id(self, assignment_id: str) -> Optional[Assignment]: ...

    @abstractmethod
    async def create(self, assignment: Assignment) -> Assignment: ...

    @abstractmethod
    async def update(self, assignment: Assignment) -> Assignment: ...

    @abstractmethod
    async def delete(self, assignment_id: str) -> None: ...


class ISubmissionRepository(ABC):
    """Контракт доступа к решениям кандидатов."""

    @abstractmethod
    async def get_all(self) -> List[Submission]: ...

    @abstractmethod
    async def get_by_id(self, submission_id: str) -> Optional[Submission]: ...

    @abstractmethod
    async def create(self, submission: Submission) -> Submission: ...

    @abstractmethod
    async def update_status(self, submission_id: str, status: str) -> None: ...

    @abstractmethod
    async def update_verdict(self, submission_id: str, verdict: str) -> None: ...

    @abstractmethod
    async def save_ai_review(self, submission_id: str, review: str) -> None: ...


class ICheckResultRepository(ABC):
    """Контракт доступа к результатам чекеров."""

    @abstractmethod
    async def get_by_submission(self, submission_id: str) -> List[CheckResult]: ...

    @abstractmethod
    async def create_many(self, results: List[CheckResult]) -> None: ...
