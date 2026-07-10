#!/usr/bin/env bash
set -Eeuo pipefail

APP_NAME="puravida-api"
APP_DIR="/opt/puravida-api"
JAR_NAME="pura-vida-backend.jar"
SERVICE_NAME="puravida-api"

SOURCE_JAR="${1:-/tmp/${JAR_NAME}}"
TARGET_JAR="${APP_DIR}/${JAR_NAME}"

if [[ ! -f "${SOURCE_JAR}" ]]; then
  echo "ERROR: No se encontró el JAR de origen: ${SOURCE_JAR}" >&2
  echo "Uso: $0 [/ruta/al/${JAR_NAME}]" >&2
  exit 1
fi

if ! id "${APP_NAME}" >/dev/null 2>&1; then
  echo "ERROR: El usuario del servicio '${APP_NAME}' no existe." >&2
  echo "Crea el usuario antes de ejecutar este script." >&2
  exit 1
fi

if [[ "${EUID}" -eq 0 ]]; then
  SUDO=()
else
  SUDO=(sudo)
fi

echo "Instalando ${JAR_NAME} en ${APP_DIR}..."
"${SUDO[@]}" install -d -o "${APP_NAME}" -g "${APP_NAME}" -m 0755 "${APP_DIR}"
"${SUDO[@]}" install -o "${APP_NAME}" -g "${APP_NAME}" -m 0644 "${SOURCE_JAR}" "${TARGET_JAR}"

echo "Recargando systemd y reiniciando ${SERVICE_NAME}..."
"${SUDO[@]}" systemctl daemon-reload
"${SUDO[@]}" systemctl restart "${SERVICE_NAME}"

echo "Estado actual de ${SERVICE_NAME}:"
"${SUDO[@]}" systemctl status "${SERVICE_NAME}" --no-pager
