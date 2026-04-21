#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./bugboard-backend/mvnw -q -f ./bugboard-backend/pom.xml -DskipTests clean package
podman compose --env-file ./db/.env up -d --build app db
podman compose --env-file ./db/.env ps
podman logs --tail 120 bugboard26-app-1
