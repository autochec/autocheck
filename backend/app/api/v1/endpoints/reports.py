"""
ReportController — статистика и отчёты.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
from fastapi import APIRouter, Depends
from app.core.response import ResponseWrapper
from app.core.security import get_current_user_id

logger = logging.getLogger(__name__)
router = APIRouter()


@router.get("/stats")
async def stats(user_id: str = Depends(get_current_user_id)) -> ResponseWrapper:
    """Общая статистика: всего проверок, средний балл, % прохождения."""
    logger.info("[ReportController]: Статистика запрошена")
    return ResponseWrapper.ok(data={
        "total": 0,
        "avg_score": 0.0,
        "pass_rate": 0.0,
        "daily": [],
    })
