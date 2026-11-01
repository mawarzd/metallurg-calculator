#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

OUT="$ROOT/dist/metallurg-calculator"
rm -rf "$OUT"
mkdir -p "$OUT/docs/diagrams" "$OUT/samples" "$OUT/scripts"

echo "Сборка и тесты..."
mvn -q clean test package

cp -R src pom.xml README.md "$OUT/"
cp -R docs/diagrams "$OUT/docs/"
cp -R samples/* "$OUT/samples/"
cp scripts/run.sh scripts/run.bat "$OUT/scripts/"
chmod +x "$OUT/scripts/run.sh"

cat > "$OUT/INSTALL.txt" <<'EOF'
Калькулятор металлурга
======================

Нужны JDK 21 и Maven 3.9.

macOS и Linux:  ./scripts/run.sh
Windows:        scripts\run.bat

Либо из корня:  mvn javafx:run

Вход: admin / admin
EOF

(
  cd "$ROOT/dist"
  rm -f metallurg-calculator.zip
  zip -qr metallurg-calculator.zip metallurg-calculator
)

echo "Готово: $ROOT/dist/metallurg-calculator.zip"
