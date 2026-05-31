"""
Submission — решение кандидата (ZIP или Git URL).
CheckResult — результат одного чекера.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
from typing import List, Optional


class SubmissionStatus(str, Enum):
    PENDING = "pending"
    RUNNING = "running"
    DONE    = "done"
    ERROR   = "error"


class CheckStatus(str, Enum):
    PASSED = "passed"
    FAILED = "failed"
    ERROR  = "error"


@dataclass
class CheckResult:
    """
    Результат выполнения одного чекера.

    Поля:
        id:            UUID результата.
        submission_id: К какому решению относится.
        checker:       Имя чекера (static_analysis, build и т.д.).
        status:        passed | failed | error.
        score:         Балл 0–100.
        message:       Краткое сообщение.
        details:       Полный вывод инструмента.
    """
    id: str
    submission_id: str
    checker: str
    status: CheckStatus
    score: float
    message: str
    details: str
    created_at: datetime = field(default_factory=datetime.utcnow)


@dataclass
class Submission:
    """
    Решение кандидата на тестовое задание.

    Поля:
        id:            UUID решения.
        assignment_id: ID задания.
        candidate_id:  ID кандидата.
        source_type:   "zip" | "git".
        source_path:   Путь к файлу или git URL.
        status:        Текущий статус проверки.
        total_score:   Итоговый взвешенный балл (после завершения).
        verdict:       Решение Эксперта: "accepted" | "rejected" | None.
        ai_review:     Текст AI-анализа.
    """
    id: str
    assignment_id: str
    candidate_id: str
    source_type: str
    source_path: str
    status: SubmissionStatus
    total_score: Optional[float] = None
    verdict: Optional[str] = None
    ai_review: Optional[str] = None
    results: List[CheckResult] = field(default_factory=list)
    created_at: datetime = field(default_factory=datetime.utcnow)
