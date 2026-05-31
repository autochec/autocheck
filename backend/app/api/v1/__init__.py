from fastapi import APIRouter
from app.api.v1.endpoints import auth, assignments, submissions, candidates, reports, realtime

router = APIRouter()
router.include_router(auth.router,        prefix="/auth",        tags=["Auth"])
router.include_router(assignments.router, prefix="/assignments", tags=["Assignments"])
router.include_router(submissions.router, prefix="/submissions", tags=["Submissions"])
router.include_router(candidates.router,  prefix="/candidates",  tags=["Candidates"])
router.include_router(reports.router,     prefix="/reports",     tags=["Reports"])
router.include_router(realtime.router,                           tags=["Realtime"])
