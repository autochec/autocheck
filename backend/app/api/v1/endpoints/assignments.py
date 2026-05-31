"""
AssignmentController — CRUD тестовых заданий.
Создание доступно только Экспертам.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from fastapi import APIRouter, Depends
from app.core.response import ResponseWrapper
from app.core.security import get_current_user_id

logger = logging.getLogger(__name__)
router = APIRouter()


@router.get("")
async def list_assignments(user_id: str = Depends(get_current_user_id)) -> ResponseWrapper:
    logger.info("[AssignmentController]: Список заданий")
    return ResponseWrapper.ok(data=[])


@router.post("", status_code=201)
async def create_assignment(user_id: str = Depends(get_current_user_id)) -> ResponseWrapper:
    logger.info(f"[AssignmentController]: Создание задания — expertId={user_id}")
    return ResponseWrapper.ok(data={"id": "placeholder"})


@router.get("/{assignment_id}")
async def get_assignment(
    assignment_id: str, user_id: str = Depends(get_current_user_id)
) -> ResponseWrapper:
    logger.info(f"[AssignmentController]: Задание — id={assignment_id}")
    return ResponseWrapper.ok(data={"id": assignment_id})


@router.put("/{assignment_id}")
async def update_assignment(
    assignment_id: str, user_id: str = Depends(get_current_user_id)
) -> ResponseWrapper:
    return ResponseWrapper.ok(data={"id": assignment_id})


@router.delete("/{assignment_id}", status_code=204)
async def delete_assignment(
    assignment_id: str, user_id: str = Depends(get_current_user_id)
) -> None:
    logger.info(f"[AssignmentController]: Удаление — id={assignment_id}")
