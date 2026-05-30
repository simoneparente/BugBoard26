#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./bugboard-backend/mvnw -q -f ./bugboard-backend/pom.xml -DskipTests clean package
podman compose up -d --build backend frontend db
podman compose ps
podman logs --tail 120 bugboard26-frontend-1
podman logs --tail 120 bugboard26-backend-1
