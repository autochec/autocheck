"""
RealtimeController — real-time обновления статуса проверки.
Реализованы оба варианта: WebSocket и Server-Sent Events (SSE).
Клиент сам выбирает удобный протокол.

Дата создания: 30-05-2025
Автор: Команда №2
"""
import asyncio
import json
import logging

from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from fastapi.responses import StreamingResponse

from app.core.config import settings
from app.infrastructure.db.base import AsyncSessionLocal
from app.infrastructure.repositories.submission_repository import SubmissionRepository

logger = logging.getLogger(__name__)
router = APIRouter()

POLL_INTERVAL = 2.0   # секунды между опросами БД
MAX_WAIT = 600        # максимум 10 минут ожидания


@router.websocket("/ws/submissions/{submission_id}/status")
async def ws_submission_status(websocket: WebSocket, submission_id: str) -> None:
    """
    WebSocket — шлёт обновления статуса проверки каждые 2 секунды.
    Закрывает соединение когда статус: done | error.

    Подключение: ws://host/api/v1/ws/submissions/{id}/status
    Сообщения: {"status": "running", "score": null} | {"status": "done", "score": 87.5}
    """
    await websocket.accept()
    logger.info(
        f"[RealtimeController]: WebSocket подключён — submissionId={submission_id}"
    )
    elapsed = 0.0
    try:
        while elapsed < MAX_WAIT:
            async with AsyncSessionLocal() as session:
                repo = SubmissionRepository(session)
                submission = await repo.get_by_id(submission_id)

            if not submission:
                await websocket.send_json({"error": "Проверка не найдена"})
                break

            payload = {
                "status": submission.status.value,
                "score":  submission.total_score,
                "verdict": submission.verdict,
            }
            await websocket.send_json(payload)
            logger.debug(
                f"[RealtimeController]: WS отправлено — "
                f"submissionId={submission_id}, status={submission.status.value}"
            )

            if submission.status.value in ("done", "error"):
                logger.info(
                    f"[RealtimeController]: WS закрыт — "
                    f"submissionId={submission_id}, статус финальный"
                )
                break

            await asyncio.sleep(POLL_INTERVAL)
            elapsed += POLL_INTERVAL

    except WebSocketDisconnect:
        logger.info(
            f"[RealtimeController]: WS клиент отключился — submissionId={submission_id}"
        )
    except Exception as e:
        logger.error(f"[RealtimeController]: Ошибка WS — {e}")
    finally:
        try:
            await websocket.close()
        except Exception:
            pass


@router.get("/submissions/{submission_id}/stream")
async def sse_submission_status(submission_id: str) -> StreamingResponse:
    """
    Server-Sent Events — альтернатива WebSocket для браузеров без WS-поддержки.

    Подключение: GET /api/v1/submissions/{id}/stream
    Заголовки: Accept: text/event-stream
    События: data: {"status": "running"}\n\n
    """
    logger.info(
        f"[RealtimeController]: SSE подключён — submissionId={submission_id}"
    )

    async def event_generator():
        elapsed = 0.0
        while elapsed < MAX_WAIT:
            try:
                async with AsyncSessionLocal() as session:
                    repo = SubmissionRepository(session)
                    submission = await repo.get_by_id(submission_id)

                if not submission:
                    yield f"data: {json.dumps({'error': 'Проверка не найдена'})}\n\n"
                    return

                payload = json.dumps({
                    "status":  submission.status.value,
                    "score":   submission.total_score,
                    "verdict": submission.verdict,
                })
                yield f"data: {payload}\n\n"
                logger.debug(
                    f"[RealtimeController]: SSE отправлено — "
                    f"submissionId={submission_id}, status={submission.status.value}"
                )

                if submission.status.value in ("done", "error"):
                    yield "event: close\ndata: done\n\n"
                    return

                await asyncio.sleep(POLL_INTERVAL)
                elapsed += POLL_INTERVAL

            except Exception as e:
                logger.error(f"[RealtimeController]: Ошибка SSE — {e}")
                yield f"data: {json.dumps({'error': str(e)})}\n\n"
                return

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",   # отключаем буферизацию nginx
        },
    )
