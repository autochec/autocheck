"""
ORM-модели SQLAlchemy — маппинг доменных сущностей на таблицы БД.
Таблицы: users, assignments, submissions, check_results.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import uuid
from datetime import datetime

from sqlalchemy import (
    Boolean, Column, DateTime, Float, ForeignKey,
    String, Text, JSON, func,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.infrastructure.db.base import Base


def _uuid() -> str:
    """Генерирует UUID4 в виде строки для использования как default."""
    return str(uuid.uuid4())


class UserORM(Base):
    """
    Таблица users — пользователи системы (Эксперты и Кандидаты).

    Поля соответствуют доменной модели User.
    """
    __tablename__ = "users"

    id            = Column(String, primary_key=True, default=_uuid)
    email         = Column(String(255), unique=True, nullable=False, index=True)
    full_name     = Column(String(255), nullable=False)
    role          = Column(String(50), nullable=False)          # expert | candidate
    hashed_password = Column(String(255), nullable=False)
    created_at    = Column(DateTime, server_default=func.now(), nullable=False)

    # Связи
    assignments   = relationship("AssignmentORM", back_populates="expert",
                                 foreign_keys="AssignmentORM.expert_id")
    submissions   = relationship("SubmissionORM", back_populates="candidate",
                                 foreign_keys="SubmissionORM.candidate_id")


class AssignmentORM(Base):
    """
    Таблица assignments — тестовые задания, создаваемые Экспертами.
    checker_configs хранится как JSONB: {checker_name: {enabled, weight}}.
    """
    __tablename__ = "assignments"

    id              = Column(String, primary_key=True, default=_uuid)
    title           = Column(String(255), nullable=False)
    description     = Column(Text, nullable=False, default="")
    expert_id       = Column(String, ForeignKey("users.id"), nullable=False, index=True)
    checker_configs = Column(JSON, nullable=False, default=dict)
    created_at      = Column(DateTime, server_default=func.now(), nullable=False)

    # Связи
    expert      = relationship("UserORM", back_populates="assignments",
                               foreign_keys=[expert_id])
    submissions = relationship("SubmissionORM", back_populates="assignment")


class SubmissionORM(Base):
    """
    Таблица submissions — решения кандидатов.
    source_type: "zip" | "git"
    status:      pending | running | done | error
    verdict:     accepted | rejected | NULL
    """
    __tablename__ = "submissions"

    id            = Column(String, primary_key=True, default=_uuid)
    assignment_id = Column(String, ForeignKey("assignments.id"), nullable=False, index=True)
    candidate_id  = Column(String, ForeignKey("users.id"), nullable=False, index=True)
    source_type   = Column(String(10), nullable=False)           # zip | git
    source_path   = Column(Text, nullable=False)                 # путь или URL
    status        = Column(String(20), nullable=False, default="pending", index=True)
    total_score   = Column(Float, nullable=True)
    verdict       = Column(String(20), nullable=True)            # accepted | rejected
    ai_review     = Column(Text, nullable=True)
    created_at    = Column(DateTime, server_default=func.now(), nullable=False)

    # Связи
    assignment  = relationship("AssignmentORM", back_populates="submissions")
    candidate   = relationship("UserORM", back_populates="submissions",
                               foreign_keys=[candidate_id])
    results     = relationship("CheckResultORM", back_populates="submission",
                               cascade="all, delete-orphan")


class CheckResultORM(Base):
    """
    Таблица check_results — результаты отдельных чекеров для каждого решения.
    checker:  static_analysis | architecture | build | test | documentation | git | ai
    status:   passed | failed | error
    score:    0.0–100.0
    """
    __tablename__ = "check_results"

    id            = Column(String, primary_key=True, default=_uuid)
    submission_id = Column(String, ForeignKey("submissions.id", ondelete="CASCADE"),
                           nullable=False, index=True)
    checker       = Column(String(50), nullable=False)
    status        = Column(String(20), nullable=False)           # passed | failed | error
    score         = Column(Float, nullable=False, default=0.0)
    message       = Column(String(500), nullable=False, default="")
    details       = Column(Text, nullable=False, default="")
    created_at    = Column(DateTime, server_default=func.now(), nullable=False)

    # Связи
    submission = relationship("SubmissionORM", back_populates="results")
