#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

podman compose --env-file ./db/.env down
podman ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
