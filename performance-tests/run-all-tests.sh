#!/bin/bash

# Master script to run performance tests on all micro-apps sequentially
# This ensures isolation and consistent test conditions

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RESULTS_DIR="$SCRIPT_DIR/results"
MASTER_LOG="$RESULTS_DIR/master_$(date +%Y%m%d_%H%M%S).log"

COOLDOWN_BETWEEN_APPS=60
PRE_TEST_WARMUP=30

# Ensure results directory exists
mkdir -p "$RESULTS_DIR"

# Define all applications to test
# Format: "app_directory:port:health_endpoint:db_endpoint:has_database"
declare -a APPS=(
    "light4j-rest-app:8080:/health:/api/users:true"
    "vertx-lite:8081:/api/v1/health:/api/v1/projects:true"
    "micronaut-rest:8082:/health:/projects:true"
    "quarkus-rest:8083:/health:/projects:true"
    "helidon-rest:8084:/health:/projects:true"
    "javalin-rest:8084:/health:/projects:true"
    "armeria-rest:8085:/health:/projects:true"
    "netty-baseline:8086:/health::false"
    "activej-rest:8087:/health:/projects:true"
    "undertow-baseline:8088:/health::false"
)

# Functions
log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')]${NC} $1" | tee -a "$MASTER_LOG"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$MASTER_LOG"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$MASTER_LOG"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$MASTER_LOG"
}

section() {
    echo "" | tee -a "$MASTER_LOG"
    echo -e "${CYAN}========================================${NC}" | tee -a "$MASTER_LOG"
    echo -e "${CYAN}$1${NC}" | tee -a "$MASTER_LOG"
    echo -e "${CYAN}========================================${NC}" | tee -a "$MASTER_LOG"
}

cleanup_all() {
    info "Cleaning up all containers..."
    cd "$PROJECT_ROOT"

    for app_config in "${APPS[@]}"; do
        IFS=':' read -r app_dir _ _ _ _ <<< "$app_config"
        if [ -d "$app_dir" ] && [ -f "$app_dir/docker-compose.yml" ]; then
            info "Stopping $app_dir..."
            (cd "$app_dir" && docker compose down -v 2>/dev/null || true)
        fi
    done

    # Additional cleanup
    docker system prune -f > /dev/null 2>&1 || true
    log "✓ Cleanup complete"
}

check_system_ready() {
    info "Checking system readiness..."

    # Check if any test ports are in use
    local ports=(8080 8081 8082 8083 8084 8085 8086 8087 8088)
    for port in "${ports[@]}"; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            warn "Port $port is in use. Attempting cleanup..."
            cleanup_all
            sleep 5
            if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
                error "Port $port still in use after cleanup. Please check manually."
                exit 1
            fi
        fi
    done

    # Check system load
    local load=$(uptime | awk -F'load average:' '{print $2}' | awk -F, '{print $1}' | xargs)
    info "System load: $load"

    # Check available memory
    local available_mem=$(free -m | awk '/^Mem:/{print $7}')
    info "Available memory: ${available_mem}MB"

    if [ "$available_mem" -lt 2000 ]; then
        warn "Low memory available. Results may be inconsistent."
    fi

    log "✓ System ready"
}

start_app() {
    local app_dir=$1
    local port=$2

    info "Starting $app_dir..."

    cd "$PROJECT_ROOT/$app_dir"

    if [ ! -f "docker-compose.yml" ]; then
        error "docker-compose.yml not found in $app_dir"
        return 1
    fi

    # Start containers
    docker compose up -d --build 2>&1 | tee -a "$MASTER_LOG"

    # Wait a bit for containers to initialize
    sleep 10

    log "✓ $app_dir started"
}

stop_app() {
    local app_dir=$1

    info "Stopping $app_dir..."

    cd "$PROJECT_ROOT/$app_dir"

    docker compose down -v 2>&1 | tee -a "$MASTER_LOG"

    log "✓ $app_dir stopped"
}

restart_postgres() {
    local app_dir=$1

    info "Restarting PostgreSQL for $app_dir..."

    cd "$PROJECT_ROOT/$app_dir"

    # Restart only the postgres service
    docker compose restart postgres 2>&1 | tee -a "$MASTER_LOG"

    # Wait for postgres to be healthy
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if docker compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
            log "✓ PostgreSQL is ready"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done

    warn "PostgreSQL health check timeout (but continuing anyway)"
    return 0
}

test_app() {
    local app_dir=$1
    local port=$2
    local health_endpoint=$3
    local db_endpoint=$4

    section "Testing: $app_dir"

    # Extract app name from directory
    local app_name=$(basename "$app_dir")

    # Run the test script
    cd "$SCRIPT_DIR"
    ./test-app.sh "$app_name" "$port" "$health_endpoint" "$db_endpoint" 2>&1 | tee -a "$MASTER_LOG"

    if [ $? -eq 0 ]; then
        log "✓ $app_name tests completed successfully"
        return 0
    else
        error "$app_name tests failed"
        return 1
    fi
}

cooldown() {
    local duration=$1
    info "Cooling down for ${duration}s..."

    for i in $(seq $duration -10 1); do
        if [ $((i % 10)) -eq 0 ]; then
            echo -n "$i..."
        fi
        sleep 10
    done
    echo ""

    # Quick system cleanup
    docker system prune -f > /dev/null 2>&1 || true

    log "✓ Cooldown complete"
}

print_summary() {
    section "Test Execution Summary"

    log "Total applications tested: ${#COMPLETED_APPS[@]}"
    log "Successful: ${#COMPLETED_APPS[@]}"
    log "Failed: ${#FAILED_APPS[@]}"

    if [ ${#COMPLETED_APPS[@]} -gt 0 ]; then
        echo "" | tee -a "$MASTER_LOG"
        log "Completed applications:"
        for app in "${COMPLETED_APPS[@]}"; do
            log "  ✓ $app"
        done
    fi

    if [ ${#FAILED_APPS[@]} -gt 0 ]; then
        echo "" | tee -a "$MASTER_LOG"
        error "Failed applications:"
        for app in "${FAILED_APPS[@]}"; do
            error "  ✗ $app"
        done
    fi

    echo "" | tee -a "$MASTER_LOG"
    log "Results directory: $RESULTS_DIR"
    log "Master log: $MASTER_LOG"
}

# Main execution
main() {
    section "Java Frameworks Performance Test Suite"
    log "Starting comprehensive performance testing..."
    log "Master log: $MASTER_LOG"

    # Arrays to track results
    COMPLETED_APPS=()
    FAILED_APPS=()

    # Initial cleanup
    cleanup_all
    sleep 5

    # System check
    check_system_ready

    # Test each application
    local total=${#APPS[@]}
    local current=1

    for app_config in "${APPS[@]}"; do
        # Parse configuration
        IFS=':' read -r app_dir port health_endpoint db_endpoint has_db <<< "$app_config"

        section "Application $current of $total: $app_dir"

        # Start the application
        if start_app "$app_dir" "$port"; then
            # Restart PostgreSQL if the app uses a database
            if [ "$has_db" = "true" ]; then
                restart_postgres "$app_dir"
                sleep 5
            fi

            # Wait for warmup
            sleep $PRE_TEST_WARMUP

            # Run tests
            if test_app "$app_dir" "$port" "$health_endpoint" "$db_endpoint"; then
                COMPLETED_APPS+=("$app_dir")
            else
                FAILED_APPS+=("$app_dir")
            fi

            # Stop the application
            stop_app "$app_dir"

            # Cooldown before next app (except for last app)
            if [ $current -lt $total ]; then
                cooldown $COOLDOWN_BETWEEN_APPS
            fi
        else
            error "Failed to start $app_dir"
            FAILED_APPS+=("$app_dir")

            # Try to stop anyway
            stop_app "$app_dir" || true

            # Cooldown before next app
            if [ $current -lt $total ]; then
                cooldown $COOLDOWN_BETWEEN_APPS
            fi
        fi

        current=$((current + 1))
    done

    # Final cleanup
    cleanup_all

    # Print summary
    print_summary

    # Generate consolidated report
    section "Generating Consolidated Report"
    if [ -x "$SCRIPT_DIR/analyze-results.sh" ]; then
        cd "$SCRIPT_DIR"
        ./analyze-results.sh 2>&1 | tee -a "$MASTER_LOG"
    else
        warn "analyze-results.sh not found or not executable"
    fi

    section "All Tests Complete!"
    log "Check $RESULTS_DIR for detailed results"

    # Exit with appropriate code
    if [ ${#FAILED_APPS[@]} -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# Trap Ctrl+C and cleanup
trap cleanup_all EXIT INT TERM

# Run main
main "$@"
