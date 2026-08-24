#!/bin/bash

# Cleanup script to stop all containers and remove volumes
# Use this to reset the environment before running tests

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# All application directories
APPS=(
    "light4j-rest-app"
    "vertx-lite"
    "micronaut-rest"
    "quarkus-rest"
    "helidon-rest"
    "javalin-rest"
    "armeria-rest"
    "netty-baseline"
    "activej-rest"
    "undertow-baseline"
    "spring-lite"
)

log "Stopping all micro-app containers..."

cd "$PROJECT_ROOT"

for app in "${APPS[@]}"; do
    if [ -d "$app" ] && [ -f "$app/docker-compose.yml" ]; then
        log "Stopping $app..."
        (cd "$app" && docker compose down -v 2>&1 | grep -v "No resource found" || true)
    fi
done

log "Pruning Docker system..."
docker system prune -f > /dev/null 2>&1 || true

# Check for any remaining containers on test ports
PORTS=(8080 8081 8082 8083 8084 8085 8086 8087 8088)

for port in "${PORTS[@]}"; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        warn "Port $port is still in use"
        lsof -Pi :$port -sTCP:LISTEN
    fi
done

log "✓ Cleanup complete"
