#!/usr/bin/env bash
set -euo pipefail
echo "Starting BugBoard stack..."

echo "📂 Moving to project directory..."
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "📦 Compiling backend..."
./bugboard-backend/mvnw -q -f ./bugboard-backend/pom.xml -DskipTests clean package
echo "📦 Compiling frontend..."
podman compose up -d --build backend frontend db
podman compose ps
podman logs --tail 120 bugboard26-frontend-1
podman logs --tail 120 bugboard26-backend-1

echo "✅ BugBoard stack started successfully!"
