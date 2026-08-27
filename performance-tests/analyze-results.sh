#!/bin/bash

# Script to analyze and aggregate performance test results
# Generates comprehensive comparison reports

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="$SCRIPT_DIR/results"
SUMMARY_MD="$RESULTS_DIR/summary.md"
SUMMARY_JSON="$RESULTS_DIR/summary.json"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Parse a value and extract just the number
extract_number() {
    local value=$1
    # Remove commas and extract number
    echo "$value" | sed 's/,//g' | grep -oE '[0-9]+\.?[0-9]*' | head -1
}

# Convert latency to microseconds for comparison
to_microseconds() {
    local value=$1

    if [[ $value == *"ms"* ]]; then
        local num=$(echo "$value" | grep -oE '[0-9]+\.?[0-9]*')
        echo "$(echo "$num * 1000" | bc)"
    elif [[ $value == *"us"* ]] || [[ $value == *"μs"* ]]; then
        echo "$value" | grep -oE '[0-9]+\.?[0-9]*'
    elif [[ $value == *"s"* ]]; then
        local num=$(echo "$value" | grep -oE '[0-9]+\.?[0-9]*')
        echo "$(echo "$num * 1000000" | bc)"
    else
        # Assume milliseconds if no unit
        local num=$(echo "$value" | grep -oE '[0-9]+\.?[0-9]*')
        echo "$(echo "$num * 1000" | bc 2>/dev/null || echo "0")"
    fi
}

# Format microseconds to readable format
format_latency() {
    local us=$1

    if [ -z "$us" ] || [ "$us" == "0" ]; then
        echo "N/A"
        return
    fi

    local num_us=$(echo "$us" | grep -oE '[0-9]+\.?[0-9]*')

    if (( $(echo "$num_us >= 1000" | bc -l) )); then
        local ms=$(echo "scale=2; $num_us / 1000" | bc)
        echo "${ms}ms"
    else
        echo "${num_us}μs"
    fi
}

# Generate markdown summary
generate_markdown_summary() {
    log "Generating markdown summary..."

    cat > "$SUMMARY_MD" << 'EOF'
# Java Frameworks Performance Test Results

**Test Date**: TIMESTAMP_PLACEHOLDER
**Tool**: wrk (HTTP benchmarking tool)
**Test Duration**: 30 seconds per scenario
**Environment**: Local development machine

## Executive Summary

This report contains comprehensive performance benchmarks for all micro-applications in this repository. Each application was tested in isolation with proper warmup and cooldown periods to ensure accurate and fair comparisons.

## Test Scenarios

1. **Health Check - Low Concurrency** (2 threads, 10 connections)
   - Tests raw endpoint performance with minimal contention
   - Measures baseline throughput and latency

2. **Health Check - Medium Concurrency** (4 threads, 100 connections)
   - Tests scalability under moderate load
   - Evaluates connection pooling and thread management

3. **Health Check - High Concurrency** (8 threads, 500 connections)
   - Stress tests maximum throughput capacity
   - Identifies performance degradation under heavy load

4. **Database Query - Low Concurrency** (2 threads, 10 connections)
   - Tests database I/O performance
   - Measures query execution efficiency

5. **Database Query - Medium Concurrency** (4 threads, 50 connections)
   - Tests database connection pooling
   - Evaluates concurrent query handling

## Results Overview

### Health Endpoint Performance Comparison

| Application | Low Concurrency | Medium Concurrency | High Concurrency |
|-------------|-----------------|--------------------|--------------------|
EOF

    # Add each app's results
    cd "$RESULTS_DIR"
    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            local app_name=$(basename "$json_file" .json)

            # Try to extract data using basic parsing (since jq might not be available)
            if [ -f "$json_file" ]; then
                # Extract throughput values from different test scenarios
                local health_low=$(grep -A 20 '"health_low_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)
                local health_med=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)
                local health_high=$(grep -A 20 '"health_high_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)

                # Format with commas for readability
                health_low=$(printf "%'.0f" "$(extract_number "$health_low")" 2>/dev/null || echo "N/A")
                health_med=$(printf "%'.0f" "$(extract_number "$health_med")" 2>/dev/null || echo "N/A")
                health_high=$(printf "%'.0f" "$(extract_number "$health_high")" 2>/dev/null || echo "N/A")

                echo "| **$app_name** | ${health_low} req/s | ${health_med} req/s | ${health_high} req/s |" >> "$SUMMARY_MD"
            fi
        fi
    done

    cat >> "$SUMMARY_MD" << 'EOF'

### Database Query Performance Comparison

| Application | Low Concurrency | Medium Concurrency | Avg Latency |
|-------------|-----------------|--------------------|--------------------|
EOF

    # Add database results
    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            local app_name=$(basename "$json_file" .json)

            if [ -f "$json_file" ]; then
                local db_low=$(grep -A 20 '"db_low_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)
                local db_med=$(grep -A 20 '"db_medium_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)
                local db_latency=$(grep -A 20 '"db_low_concurrency"' "$json_file" | grep '"avg_latency"' | cut -d'"' -f4 | head -1)

                if [ -n "$db_low" ] && [ "$db_low" != "{}" ]; then
                    db_low=$(printf "%'.0f" "$(extract_number "$db_low")" 2>/dev/null || echo "N/A")
                    db_med=$(printf "%'.0f" "$(extract_number "$db_med")" 2>/dev/null || echo "N/A")

                    echo "| **$app_name** | ${db_low} req/s | ${db_med} req/s | ${db_latency} |" >> "$SUMMARY_MD"
                fi
            fi
        fi
    done

    cat >> "$SUMMARY_MD" << 'EOF'

### Latency Comparison (Health Endpoint - Medium Concurrency)

| Application | Average | p50 | p90 | p99 | Max |
|-------------|---------|-----|-----|-----|-----|
EOF

    # Add latency comparisons
    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            local app_name=$(basename "$json_file" .json)

            if [ -f "$json_file" ]; then
                local avg=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"avg_latency"' | cut -d'"' -f4 | head -1)
                local p50=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"p50"' | cut -d'"' -f4 | head -1)
                local p90=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"p90"' | cut -d'"' -f4 | head -1)
                local p99=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"p99"' | cut -d'"' -f4 | head -1)
                local max=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"max_latency"' | cut -d'"' -f4 | head -1)

                # Handle empty values
                avg=${avg:-"N/A"}
                p50=${p50:-"N/A"}
                p90=${p90:-"N/A"}
                p99=${p99:-"N/A"}
                max=${max:-"N/A"}

                echo "| **$app_name** | $avg | $p50 | $p90 | $p99 | $max |" >> "$SUMMARY_MD"
            fi
        fi
    done

    cat >> "$SUMMARY_MD" << 'EOF'

## Key Findings

### Highest Throughput
The following applications achieved the highest throughput in each category:

**Health Endpoint (Simple Requests)**:
EOF

    # Find highest throughput for health endpoint
    local max_throughput=0
    local max_app=""

    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            local app_name=$(basename "$json_file" .json)
            local throughput=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)

            if [ -n "$throughput" ]; then
                local throughput_num=$(extract_number "$throughput")
                if (( $(echo "$throughput_num > $max_throughput" | bc -l 2>/dev/null || echo "0") )); then
                    max_throughput=$throughput_num
                    max_app=$app_name
                fi
            fi
        fi
    done

    if [ -n "$max_app" ]; then
        local formatted_throughput=$(printf "%'.0f" "$max_throughput")
        echo "- **$max_app**: ${formatted_throughput} req/s" >> "$SUMMARY_MD"
    fi

    cat >> "$SUMMARY_MD" << 'EOF'

**Database Queries**:
EOF

    # Find highest throughput for database queries
    max_throughput=0
    max_app=""

    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            local app_name=$(basename "$json_file" .json)
            local throughput=$(grep -A 20 '"db_low_concurrency"' "$json_file" | grep '"throughput"' | cut -d'"' -f4 | head -1)

            if [ -n "$throughput" ] && [ "$throughput" != "{}" ]; then
                local throughput_num=$(extract_number "$throughput")
                if (( $(echo "$throughput_num > $max_throughput" | bc -l 2>/dev/null || echo "0") )); then
                    max_throughput=$throughput_num
                    max_app=$app_name
                fi
            fi
        fi
    done

    if [ -n "$max_app" ]; then
        local formatted_throughput=$(printf "%'.0f" "$max_throughput")
        echo "- **$max_app**: ${formatted_throughput} req/s" >> "$SUMMARY_MD"
    fi

    cat >> "$SUMMARY_MD" << 'EOF'

### Lowest Latency
Applications with the best latency characteristics:

EOF

    # Find lowest average latency
    local min_latency=999999999
    local min_app=""

    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            local app_name=$(basename "$json_file" .json)
            local latency=$(grep -A 20 '"health_medium_concurrency"' "$json_file" | grep '"avg_latency"' | cut -d'"' -f4 | head -1)

            if [ -n "$latency" ]; then
                local latency_us=$(to_microseconds "$latency")
                if [ -n "$latency_us" ] && (( $(echo "$latency_us > 0 && $latency_us < $min_latency" | bc -l 2>/dev/null || echo "0") )); then
                    min_latency=$latency_us
                    min_app=$app_name
                    min_latency_formatted=$latency
                fi
            fi
        fi
    done

    if [ -n "$min_app" ]; then
        echo "- **$min_app**: ${min_latency_formatted} average latency" >> "$SUMMARY_MD"
    fi

    cat >> "$SUMMARY_MD" << 'EOF'

## Analysis by Category

### Traditional Frameworks (Spring-like)
- Micronaut
- Quarkus
- Helidon

These frameworks provide comprehensive features with good performance balance.

### Lightweight/Micro Frameworks
- Javalin
- Light4J

Focused on minimal overhead and maximum performance.

### Reactive Frameworks
- Vert.x

Event-driven, non-blocking architecture for high concurrency.

### Pure Performance
- Netty Baseline
- Undertow Baseline
- Armeria
- ActiveJ

Raw framework performance without additional abstractions.

## Recommendations

### Choose Based on Use Case

**For Maximum Throughput**:
Check the "Highest Throughput" section above for the best performers.

**For Low Latency**:
Check the "Lowest Latency" section above for the best performers.

**For Database-Heavy Applications**:
Reactive frameworks (Vert.x) typically excel at I/O-bound workloads.

**For Simplicity**:
Lightweight frameworks (Javalin, Light4J) offer good performance with minimal complexity.

**For Enterprise Features**:
Traditional frameworks (Micronaut, Quarkus, Helidon) provide comprehensive tooling.

## Test Methodology

### Isolation
- Each application tested completely independently
- All other containers stopped during testing
- 60-second cooldown between applications
- Docker cleanup between tests

### Warmup
- 100 warmup requests sent before each test
- 30-second JIT compilation period
- Health check validation before testing

### Measurement
- 30-second test duration per scenario
- Multiple concurrency levels tested
- Both simple and database endpoints tested
- Latency percentiles measured (p50, p75, p90, p99)

### Consistency
- Same test machine for all applications
- Same test parameters across all apps
- Identical warmup procedures
- Controlled cooldown periods

## Detailed Results

For detailed per-application results, see individual result files:
EOF

    # List all result files
    for md_file in *.md; do
        if [ "$md_file" != "summary.md" ] && [ -f "$md_file" ]; then
            local app_name=$(basename "$md_file" .md)
            echo "- [$app_name](./$md_file)" >> "$SUMMARY_MD"
        fi
    done

    cat >> "$SUMMARY_MD" << 'EOF'

## Conclusion

All applications demonstrate production-ready performance characteristics with different trade-offs:

- **Throughput-focused**: Applications optimized for handling high request volumes
- **Latency-focused**: Applications optimized for quick response times
- **Balance**: Applications offering good all-round performance
- **Feature-rich**: Applications with comprehensive frameworks and tooling

The best choice depends on your specific requirements: throughput, latency, features, or team expertise.

---

*Generated on TIMESTAMP_PLACEHOLDER*
EOF

    # Replace timestamp
    sed -i "s/TIMESTAMP_PLACEHOLDER/$(date '+%Y-%m-%d %H:%M:%S')/g" "$SUMMARY_MD"

    log "✓ Markdown summary generated: $SUMMARY_MD"
}

# Generate JSON summary (simplified without jq)
generate_json_summary() {
    log "Generating JSON summary..."

    echo "{" > "$SUMMARY_JSON"
    echo "  \"timestamp\": \"$(date -Iseconds)\"," >> "$SUMMARY_JSON"
    echo "  \"applications\": [" >> "$SUMMARY_JSON"

    local first=true
    cd "$RESULTS_DIR"
    for json_file in *.json; do
        if [ "$json_file" != "summary.json" ] && [ -f "$json_file" ]; then
            if [ "$first" = true ]; then
                first=false
            else
                echo "    ," >> "$SUMMARY_JSON"
            fi

            cat "$json_file" | sed 's/^/    /' >> "$SUMMARY_JSON"
        fi
    done

    echo "" >> "$SUMMARY_JSON"
    echo "  ]" >> "$SUMMARY_JSON"
    echo "}" >> "$SUMMARY_JSON"

    log "✓ JSON summary generated: $SUMMARY_JSON"
}

# Main execution
main() {
    log "Analyzing performance test results..."

    if [ ! -d "$RESULTS_DIR" ]; then
        echo "Results directory not found: $RESULTS_DIR"
        exit 1
    fi

    # Check if we have any JSON results
    local json_count=$(ls -1 "$RESULTS_DIR"/*.json 2>/dev/null | grep -v summary.json | wc -l)

    if [ "$json_count" -eq 0 ]; then
        echo "No test results found in $RESULTS_DIR"
        exit 1
    fi

    log "Found $json_count application result(s)"

    # Generate reports
    generate_markdown_summary
    generate_json_summary

    log "✓ Analysis complete"
    log "Summary: $SUMMARY_MD"
    log "JSON: $SUMMARY_JSON"
}

main "$@"
