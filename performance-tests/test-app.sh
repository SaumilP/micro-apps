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

# Test parameters
WARMUP_REQUESTS=100
WARMUP_DURATION=30
TEST_DURATION=30
COOLDOWN_DURATION=60

# Ensure results directory exists
mkdir -p "$RESULTS_DIR"

# Log file
LOG_FILE="$RESULTS_DIR/${APP_NAME}_${TIMESTAMP}.log"

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
    local url=$1
    info "Warming up: sending $WARMUP_REQUESTS requests"

    for i in $(seq 1 $WARMUP_REQUESTS); do
        curl -sf "$url" > /dev/null 2>&1 || true
        if [ $((i % 20)) -eq 0 ]; then
            echo -n "."
        fi
    done
    echo ""

    info "Warmup complete. Waiting ${WARMUP_DURATION}s for JIT compilation..."
    sleep $WARMUP_DURATION
}

run_wrk_test() {
    local test_name=$1
    local url=$2
    local threads=$3
    local connections=$4
    local duration=$5

    info "Running: $test_name"
    info "  URL: $url"
    info "  Threads: $threads, Connections: $connections, Duration: ${duration}s"

    local output_file="$RESULTS_DIR/${APP_NAME}_${test_name// /_}_${TIMESTAMP}.txt"

    wrk -t$threads -c$connections -d${duration}s --latency "$url" > "$output_file" 2>&1

    # Parse results
    local throughput=$(grep "Requests/sec:" "$output_file" | awk '{print $2}')
    local avg_latency=$(grep "Latency" "$output_file" | head -1 | awk '{print $2}')
    local max_latency=$(grep "Latency" "$output_file" | head -1 | awk '{print $4}')

    log "  ✓ Throughput: $throughput req/s"
    log "  ✓ Latency (avg): $avg_latency"
    log "  ✓ Latency (max): $max_latency"

    echo "$output_file"
}

parse_wrk_output() {
    local file=$1

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
  "health_endpoint": "$HEALTH_ENDPOINT",
  "db_endpoint": "$DB_ENDPOINT",
  "scenarios": {
    "health_low_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_Low_Concurrency_${TIMESTAMP}.txt"),
    "health_medium_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_Medium_Concurrency_${TIMESTAMP}.txt"),
    "health_high_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_High_Concurrency_${TIMESTAMP}.txt"),
    "db_low_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Database_Low_Concurrency_${TIMESTAMP}.txt" 2>/dev/null || echo "{}"),
    "db_medium_concurrency": $(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Database_Medium_Concurrency_${TIMESTAMP}.txt" 2>/dev/null || echo "{}")
  }
}
EOF

    log "JSON report generated: $json_file"
}

generate_markdown_report() {
    local md_file="$RESULTS_DIR/${APP_NAME}.md"

    # Read JSON data
    local health_low=$(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_Low_Concurrency_${TIMESTAMP}.txt")
    local health_med=$(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_Medium_Concurrency_${TIMESTAMP}.txt")
    local health_high=$(parse_wrk_output "$RESULTS_DIR/${APP_NAME}_Health_High_Concurrency_${TIMESTAMP}.txt")

    cat > "$md_file" << 'EOF'
# Performance Test Results: APP_NAME_PLACEHOLDER

**Test Date**: TIMESTAMP_PLACEHOLDER
**Port**: PORT_PLACEHOLDER
**Test Duration**: TEST_DURATION_PLACEHOLDERs per scenario

## Test Environment
- Tool: wrk
- OS: Linux
- Test Machine: Local development environment

## Results Summary

### Health Endpoint Tests

#### Low Concurrency (2 threads, 10 connections)
HEALTH_LOW_PLACEHOLDER

#### Medium Concurrency (4 threads, 100 connections)
HEALTH_MED_PLACEHOLDER

#### High Concurrency (8 threads, 500 connections)
HEALTH_HIGH_PLACEHOLDER

### Database Endpoint Tests

#### Low Concurrency (2 threads, 10 connections)
DB_LOW_PLACEHOLDER

#### Medium Concurrency (4 threads, 50 connections)
DB_MED_PLACEHOLDER

## Analysis

### Strengths
- [To be filled after analysis]

### Observations
- [To be filled after analysis]

### Recommendations
- [To be filled after analysis]
EOF

    # Replace placeholders
    sed -i "s/APP_NAME_PLACEHOLDER/$APP_NAME/g" "$md_file"
    sed -i "s/TIMESTAMP_PLACEHOLDER/$TIMESTAMP/g" "$md_file"
    sed -i "s/PORT_PLACEHOLDER/$PORT/g" "$md_file"
    sed -i "s/TEST_DURATION_PLACEHOLDER/$TEST_DURATION/g" "$md_file"

    log "Markdown report generated: $md_file"
}

# Main execution
main() {
    log "=========================================="
    log "Performance Testing: $APP_NAME"
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

    # Warmup
    warmup "$health_url"

    # Run test scenarios
    log "Starting test scenarios..."

    # Health endpoint tests
    run_wrk_test "Health Low Concurrency" "$health_url" 2 10 $TEST_DURATION
    sleep 5

    run_wrk_test "Health Medium Concurrency" "$health_url" 4 100 $TEST_DURATION
    sleep 5

    run_wrk_test "Health High Concurrency" "$health_url" 8 500 $TEST_DURATION
    sleep 10

    # Database endpoint tests (if applicable)
    if curl -sf "$db_url" > /dev/null 2>&1; then
        log "Database endpoint available, running DB tests..."

        # Warmup DB endpoint
        for i in $(seq 1 50); do
            curl -sf "$db_url" > /dev/null 2>&1 || true
        done
        sleep 10

        run_wrk_test "Database Low Concurrency" "$db_url" 2 10 $TEST_DURATION
        sleep 5

        run_wrk_test "Database Medium Concurrency" "$db_url" 4 50 $TEST_DURATION
    else
        warn "Database endpoint not available, skipping DB tests"
    fi

    # Generate reports
    log "Generating reports..."
    generate_json_report
    generate_markdown_report

    log "=========================================="
    log "✓ Testing complete: $APP_NAME"
    log "=========================================="
}

# Run main function
main "$@"
