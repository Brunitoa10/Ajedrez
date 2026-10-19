#!/usr/bin/env bash
# ==============================================================
#  Genera la versión PORTABLE para Linux / macOS (no requiere Java instalado)
#  Resultado: target/portable/Ajedrez/  (en macOS: Ajedrez.app)
#  Requisitos para compilar: JDK 21+ (incluye jpackage) y Maven.
# ==============================================================
set -euo pipefail
cd "$(dirname "$0")/.."

echo "[1/3] Compilando y ejecutando tests..."
mvn -q clean package

echo "[2/3] Creando imagen portable con jpackage..."
rm -rf target/portable
jpackage --type app-image \
  --name Ajedrez \
  --app-version 1.0.0 \
  --input target/dist \
  --main-jar ajedrez.jar \
  --main-class com.ajedrez.Launcher \
  --dest target/portable \
  --java-options "-Dfile.encoding=UTF-8" \
  --jlink-options "--strip-debug --no-header-files --no-man-pages"

echo "[3/3] Comprimiendo..."
(cd target/portable && tar czf ../Ajedrez-portable-$(uname -s | tr '[:upper:]' '[:lower:]').tar.gz .)

echo "Listo: target/portable"
