#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

if [ ! -f ".env" ]; then
  echo "ERROR: No existe el archivo .env"
  echo "Crea .env tomando como base .env.example"
  exit 1
fi

set -a
source .env
set +a

echo "Levantando PuraVida API..."
echo "DB URL: ${PURAVIDA_DB_URL}"
echo "DB USER: ${PURAVIDA_DB_USERNAME}"
echo "DB PASSWORD: ${PURAVIDA_DB_PASSWORD:+CONFIGURADA}"
echo "JWT SECRET: ${JWT_SECRET:+CONFIGURADO}"

mvn spring-boot:run
