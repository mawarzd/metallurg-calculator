#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v java >/dev/null; then
  echo "Нужен JDK 21+. Установите Java и повторите."
  exit 1
fi
if ! command -v mvn >/dev/null; then
  echo "Нужен Apache Maven 3.9+. Установите Maven и повторите."
  exit 1
fi

echo "Запуск ИС «Калькулятор металлурга»..."
mvn -q javafx:run
