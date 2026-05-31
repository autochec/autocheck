"""
DockerRunner — запускает чекеры в изолированных Docker-контейнерах.
Код кандидата НЕ исполняется в основном контейнере приложения.
Таймаут: 3 минуты на каждый чекер (требование задания).

Дата создания: 30-05-2025
Автор: Команда №2
"""
import logging
import os
import subprocess
import tempfile
import uuid

logger = logging.getLogger(__name__)

TIMEOUT_SECONDS = 180  # 3 минуты

# Образы Docker для каждого типа проекта
DOCKER_IMAGES = {
    "android": "mingc/android-build-box:latest",
    "flutter":  "ghcr.io/cirruslabs/flutter:stable",
    "ios":      "swift:5.10",
    "node":     "node:20-alpine",
    "python":   "python:3.12-slim",
    "default":  "ubuntu:22.04",
}


class DockerRunner:
    """
    Запускает произвольную команду внутри изолированного Docker-контейнера.

    Контейнер:
      - не имеет доступа к сети (--network none)
      - ограничен по памяти (--memory 512m)
      - ограничен по CPU (--cpus 1)
      - автоматически удаляется после завершения (--rm)
    """

    def run(
        self,
        command: list[str],
        source_dir: str,
        image: str = "ubuntu:22.04",
        env: dict | None = None,
    ) -> tuple[int, str, str]:
        """
        Выполняет команду внутри Docker-контейнера с монтированием кода.

        Args:
            command:    Команда для выполнения внутри контейнера.
            source_dir: Директория с кодом кандидата (монтируется как /workspace).
            image:      Docker-образ для запуска.
            env:        Дополнительные переменные окружения.
        Returns:
            Кортеж (returncode, stdout, stderr).
        """
        abs_source = os.path.abspath(source_dir)
        container_name = f"checker_{uuid.uuid4().hex[:8]}"

        docker_cmd = [
            "docker", "run",
            "--rm",
            "--name", container_name,
            "--network", "none",          # нет сети — изоляция
            "--memory", "512m",
            "--cpus", "1",
            "--read-only",                 # файловая система только для чтения
            "--tmpfs", "/tmp",             # разрешаем только /tmp на запись
            "-v", f"{abs_source}:/workspace:ro",  # код только для чтения
            "-w", "/workspace",
        ]

        # Добавляем переменные окружения
        for key, value in (env or {}).items():
            docker_cmd += ["-e", f"{key}={value}"]

        docker_cmd.append(image)
        docker_cmd.extend(command)

        logger.info(
            f"[DockerRunner]: Запуск контейнера — image={image}, "
            f"name={container_name}"
        )
        try:
            result = subprocess.run(
                docker_cmd,
                capture_output=True,
                text=True,
                timeout=TIMEOUT_SECONDS,
            )
            logger.debug(
                f"[DockerRunner]: Контейнер завершён — "
                f"name={container_name}, returncode={result.returncode}"
            )
            return result.returncode, result.stdout, result.stderr
        except subprocess.TimeoutExpired:
            logger.error(
                f"[DockerRunner]: Таймаут — Превышено время выполнения "
                f"({TIMEOUT_SECONDS}s), container={container_name}"
            )
            # Принудительно останавливаем контейнер
            subprocess.run(
                ["docker", "stop", container_name],
                capture_output=True, timeout=10,
            )
            raise
        except FileNotFoundError:
            logger.error("[DockerRunner]: Ошибка — Docker не найден на хосте")
            raise RuntimeError("Docker недоступен")
        except Exception as e:
            logger.error(f"[DockerRunner]: Ошибка — {e}")
            raise

    @staticmethod
    def detect_image(source_dir: str) -> str:
        """
        Определяет подходящий Docker-образ по файлам в директории.

        Args:
            source_dir: Директория с исходным кодом кандидата.
        Returns:
            Имя Docker-образа.
        """
        try:
            files = os.listdir(source_dir)
        except OSError:
            return DOCKER_IMAGES["default"]

        if "pubspec.yaml" in files:
            return DOCKER_IMAGES["flutter"]
        if "build.gradle" in files or "build.gradle.kts" in files:
            return DOCKER_IMAGES["android"]
        if any(f.endswith(".xcodeproj") for f in files):
            return DOCKER_IMAGES["ios"]
        if "package.json" in files:
            return DOCKER_IMAGES["node"]
        return DOCKER_IMAGES["default"]
