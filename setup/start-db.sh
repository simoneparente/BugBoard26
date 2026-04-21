#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

podman compose --env-file ./db/.env up -d db
podman compose --env-file ./db/.env ps
podman ps --filter name=bugboard26-db-1 --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
