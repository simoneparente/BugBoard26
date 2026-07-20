#!/usr/bin/env bash
set -euo pipefail
echo "Starting BugBoard backend..."

echo "📂 Moving to project directory"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "📦 Compiling backend..."
./bugboard-backend/mvnw -q -f ./bugboard-backend/pom.xml -DskipTests clean package

echo "📦 Starting backend and database..."
podman compose up -d --build backend db
podman compose ps
podman logs --tail 120 bugboard26-backend-1

echo "✅ BugBoard backend started successfully!"

