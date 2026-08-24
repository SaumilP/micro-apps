#!/bin/bash

# Performance testing script for individual micro-apps
# Usage: ./test-app.sh <app-name> <port> <health-endpoint> <db-endpoint>

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME=${1}
PORT=${2}
HEALTH_ENDPOINT=${3:-"/health"}
DB_ENDPOINT=${4:-"/api/projects"}
RESULTS_DIR="$(dirname "$0")/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Test parameters - All tests run concurrently for 5 minutes total
WARMUP_REQUESTS=100
WARMUP_DURATION=30
TEST_DURATION=300  # 5 minutes total for all concurrent tests

# Ensure results directory exists
mkdir -p "$RESULTS_DIR"

# Log file
LOG_FILE="$RESULTS_DIR/${APP_NAME}_${TIMESTAMP}.log"

# Arrays to store background process info
declare -a TEST_PIDS
declare -a TEST_FILES
declare -a TEST_NAMES

# Functions
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$LOG_FILE"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

check_prerequisites() {
    if ! command -v wrk &> /dev/null; then
        error "wrk is not installed. Please install it first."
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        error "docker is not installed."
        exit 1
    fi

    if ! command -v curl &> /dev/null; then
        error "curl is not installed."
        exit 1
    fi
}

wait_for_health() {
    local url=$1
    local max_attempts=60
    local attempt=0

    info "Waiting for health check at $url"

    while [ $attempt -lt $max_attempts ]; do
        if curl -sf "$url" > /dev/null 2>&1; then
            log "✓ Health check passed"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done

    error "Health check failed after $max_attempts attempts"
    return 1
}

warmup() {
    local health_url=$1
    local db_url=$2

    info "Warming up: sending $WARMUP_REQUESTS requests to each endpoint"

    # Warmup health endpoint
    for i in $(seq 1 $WARMUP_REQUESTS); do
        curl -sf "$health_url" > /dev/null 2>&1 || true
        if [ $((i % 20)) -eq 0 ]; then
            echo -n "."
        fi
    done

    # Warmup database endpoint if available
    if curl -sf "$db_url" > /dev/null 2>&1; then
        for i in $(seq 1 50); do
            curl -sf "$db_url" > /dev/null 2>&1 || true
        done
    fi

    echo ""
    info "Warmup complete. Waiting ${WARMUP_DURATION}s for JIT compilation..."
    sleep $WARMUP_DURATION
}

run_wrk_test_background() {
    local test_name=$1
    local url=$2
    local threads=$3
    local connections=$4
    local duration=$5

    info "Starting: $test_name (background)"
    info "  URL: $url"
    info "  Threads: $threads, Connections: $connections, Duration: ${duration}s"

    local output_file="$RESULTS_DIR/${APP_NAME}_${test_name// /_}_${TIMESTAMP}.txt"

    # Run wrk in background and store PID
    wrk -t$threads -c$connections -d${duration}s --latency "$url" > "$output_file" 2>&1 &
    local pid=$!

    # Store in arrays
    TEST_PIDS+=($pid)
    TEST_FILES+=("$output_file")
    TEST_NAMES+=("$test_name")
}

wait_for_tests() {
    info "Waiting for all concurrent tests to complete (${TEST_DURATION}s)..."

    # Wait for all background processes
    for i in "${!TEST_PIDS[@]}"; do
        wait ${TEST_PIDS[$i]} 2>/dev/null || true
    done

    info "All tests completed. Parsing results..."

    # Parse and display results
    for i in "${!TEST_PIDS[@]}"; do
        local output_file="${TEST_FILES[$i]}"
        local test_name="${TEST_NAMES[$i]}"

        if [ -f "$output_file" ] && [ -s "$output_file" ]; then
            local throughput=$(grep "Requests/sec:" "$output_file" | awk '{print $2}')
            local avg_latency=$(grep "Latency" "$output_file" | head -1 | awk '{print $2}')
            local max_latency=$(grep "Latency" "$output_file" | head -1 | awk '{print $4}')

            log "  ✓ $test_name - Throughput: $throughput req/s, Latency (avg): $avg_latency, (max): $max_latency"
        else
            warn "  ✗ $test_name - No results generated"
        fi
    done

    log "✓ All concurrent tests completed"
}

parse_wrk_output() {
    local file=$1

    if [ ! -f "$file" ] || [ ! -s "$file" ]; then
        echo "{}"
        return
    fi

    # Extract key metrics using awk and grep
    local requests_per_sec=$(grep "Requests/sec:" "$file" | awk '{print $2}')
    local avg_latency=$(grep "Latency" "$file" | head -1 | awk '{print $2}')
    local stdev_latency=$(grep "Latency" "$file" | head -1 | awk '{print $3}')
    local max_latency=$(grep "Latency" "$file" | head -1 | awk '{print $4}')
    local total_requests=$(grep "requests in" "$file" | awk '{print $1}')
    local total_duration=$(grep "requests in" "$file" | awk '{print $3}')

    # Percentile latencies (if available)
    local p50=$(grep "50%" "$file" | awk '{print $2}')
    local p75=$(grep "75%" "$file" | awk '{print $2}')
    local p90=$(grep "90%" "$file" | awk '{print $2}')
    local p99=$(grep "99%" "$file" | awk '{print $2}')

    # Return as JSON-like structure
    echo "{\"throughput\":\"$requests_per_sec\",\"avg_latency\":\"$avg_latency\",\"max_latency\":\"$max_latency\",\"p50\":\"$p50\",\"p75\":\"$p75\",\"p90\":\"$p90\",\"p99\":\"$p99\",\"total_requests\":\"$total_requests\"}"
}

generate_json_report() {
    local json_file="$RESULTS_DIR/${APP_NAME}.json"

    cat > "$json_file" << EOF
{
  "app_name": "$APP_NAME",
  "port": $PORT,
  "timestamp": "$TIMESTAMP",
  "test_duration": $TEST_DURATION,
  "test_type": "concurrent_multi_endpoint",
  "health_endpoint": "$HEALTH_ENDPOINT",
  "db_endpoint": "$DB_ENDPOINT",
  "scenarios": {
    "health_low_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_Low_Concurrency_${TIMESTAMP}.txt"),
    "health_medium_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_Medium_Concurrency_${TIMESTAMP}.txt"),
    "health_high_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_High_Concurrency_${TIMESTAMP}.txt"),
    "db_low_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Database_Low_Concurrency_${TIMESTAMP}.txt"),
    "db_medium_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Database_Medium_Concurrency_${TIMESTAMP}.txt")
  }
}
EOF

    log "JSON report generated: $json_file"
}

# Main execution
main() {
    log "=========================================="
    log "Performance Testing: $APP_NAME"
    log "Test Type: Concurrent Multi-Endpoint (5min total)"
    log "=========================================="

    check_prerequisites

    # Health endpoint URL
    local health_url="http://localhost:${PORT}${HEALTH_ENDPOINT}"
    local db_url="http://localhost:${PORT}${DB_ENDPOINT}"

    # Wait for app to be ready
    if ! wait_for_health "$health_url"; then
        error "Application not ready. Exiting."
        exit 1
    fi

    # Warmup both endpoints
    warmup "$health_url" "$db_url"

    # Run all test scenarios CONCURRENTLY for maximum efficiency
    log "Starting concurrent test scenarios (all running in parallel for ${TEST_DURATION}s)..."

    # Health endpoint tests at different concurrency levels (all concurrent)
    run_wrk_test_background "Health Low Concurrency" "$health_url" 2 10 $TEST_DURATION
    run_wrk_test_background "Health Medium Concurrency" "$health_url" 4 100 $TEST_DURATION
    run_wrk_test_background "Health High Concurrency" "$health_url" 8 500 $TEST_DURATION

    # Database endpoint tests (if available) - concurrent with health tests
    if curl -sf "$db_url" > /dev/null 2>&1; then
        log "Database endpoint available, running concurrent DB tests..."
        run_wrk_test_background "Database Low Concurrency" "$db_url" 2 10 $TEST_DURATION
        run_wrk_test_background "Database Medium Concurrency" "$db_url" 4 50 $TEST_DURATION
    else
        warn "Database endpoint not available, skipping DB tests"
    fi

    # Wait for all tests to complete
    wait_for_tests

    # Generate reports
    log "Generating reports..."
    generate_json_report

    log "=========================================="
    log "✓ Testing complete: $APP_NAME"
    log "  Total test time: ${TEST_DURATION}s (5 minutes)"
    log "  All endpoints tested concurrently"
    log "=========================================="
}

# Run main function
main "$@"
