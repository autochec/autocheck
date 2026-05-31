"""
CandidateController — список кандидатов и их профили.

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
async def list_candidates(user_id: str = Depends(get_current_user_id)) -> ResponseWrapper:
    logger.info("[CandidateController]: Список кандидатов")
    return ResponseWrapper.ok(data=[])


@router.get("/{candidate_id}")
async def get_candidate(
    candidate_id: str, user_id: str = Depends(get_current_user_id)
) -> ResponseWrapper:
    logger.info(f"[CandidateController]: Кандидат — id={candidate_id}")
    return ResponseWrapper.ok(data={"id": candidate_id})
