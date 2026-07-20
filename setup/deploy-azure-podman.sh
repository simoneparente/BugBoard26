#!/usr/bin/env bash
set -euo pipefail

SETUP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SETUP_DIR/.." && pwd)"

echo "========================================="
echo "⚙️  PHASE 1: BACKEND DEPLOYMENT"
echo "========================================="
cd "$ROOT_DIR/bugboard-backend"

echo "🚀 Building Spring Boot locally (via Mac network)..."
./mvnw clean package -DskipTests

echo "📦 Building Podman image (AMD64 architecture for Azure)..."
podman build --platform linux/amd64 --tls-verify=false -t bugboard26acr.azurecr.io/bugboard-backend:latest -f Dockerfile.azure .
podman push --tls-verify=false bugboard26acr.azurecr.io/bugboard-backend:latest

echo "========================================="
echo "🎨 PHASE 2: FRONTEND DEPLOYMENT"
echo "========================================="
cd "$ROOT_DIR/bugboard-frontend"

echo "🚀 Building Angular locally (via Mac network)..."
npm install
npm run build -- --configuration production

echo "📦 Building Podman image (AMD64 architecture for Azure)..."
podman build --platform linux/amd64 --tls-verify=false -t bugboard26acr.azurecr.io/bugboard-frontend:latest -f Dockerfile.azure .
podman push --tls-verify=false bugboard26acr.azurecr.io/bugboard-frontend:latest

echo "========================================="
echo "🎉 DEPLOYMENT COMPLETED IN OFFLINE-BUILD MODE!"
echo "========================================="
echo "⚠️  Go to the Azure Portal and restart 'bugboard-web' and 'bugboard-api'."