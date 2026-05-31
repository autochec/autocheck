# AutoCheckMobile — Backend

SaaS-сервис автоматизированной проверки тестовых заданий для мобильных разработчиков.

## Стек
- **FastAPI** (async REST API)
- **PostgreSQL** + SQLAlchemy (async) + Alembic (миграции)
- **Redis** + Celery (очередь задач)
- **Docker** + docker-compose

## Архитектура

```
app/
├── api/v1/endpoints/     # Controllers (HTTP-обработка)
├── application/use_cases/ # Use Cases (бизнес-сценарии)
├── domain/
│   ├── models/           # Чистые бизнес-модели (без ORM)
│   └── interfaces/       # Абстракции репозиториев и чекеров
└── infrastructure/
    ├── repositories/     # Реализации IRepository (SQLAlchemy)
    ├── checkers/         # Реализации IChecker
    └── services/         # AI-провайдер, файловое хранилище
worker/
└── tasks.py              # Celery-задачи
```

## Быстрый старт

```bash
cp .env.example .env
# Отредактируйте .env: JWT_SECRET, AI_API_KEY (опционально)

docker compose up --build
```

API доступен на http://localhost:8000/api/docs

## Миграции

```bash
alembic upgrade head
```
