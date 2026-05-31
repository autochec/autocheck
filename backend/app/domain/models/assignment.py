"""
Assignment — тестовое задание, создаваемое Экспертом.

Дата создания: 30-05-2025
Автор: Команда №2
"""
from dataclasses import dataclass, field
from datetime import datetime
from typing import Dict


@dataclass
class CheckerConfig:
    """
    Конфигурация одного чекера внутри задания.

    Поля:
        enabled: Активен ли чекер для этого задания.
        weight:  Вес чекера (0–100); сумма всех весов должна равняться 100.
    """
    enabled: bool
    weight: float


@dataclass
class Assignment:
    """
    Тестовое задание для кандидата.

    Поля:
        id:              UUID задания.
        title:           Название.
        description:     Описание требований.
        expert_id:       ID создавшего Эксперта.
        checker_configs: Словарь {checker_name: CheckerConfig}.
    """
    id: str
    title: str
    description: str
    expert_id: str
    checker_configs: Dict[str, CheckerConfig]
    created_at: datetime = field(default_factory=datetime.utcnow)
