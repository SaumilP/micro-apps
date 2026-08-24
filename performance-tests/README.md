# Performance Testing Suite

This directory contains comprehensive performance testing scripts for all micro-apps in this repository.

## Overview

The performance testing suite uses `wrk` (HTTP benchmarking tool) to measure:
- Throughput (requests/second)
- Latency (average, min, max, p50, p75, p90, p99)
- Connection handling
- Resource utilization

## Applications Under Test

| Application | Port | Database | Endpoints Tested |
|-------------|------|----------|------------------|
| light4j-rest-app | 8080 | PostgreSQL | /health, /api/users |
| vertx-lite | 8081 | MySQL | /api/v1/health, /api/v1/projects |
| micronaut-rest | 8082 | PostgreSQL | /health, /projects |
| quarkus-rest | 8083 | PostgreSQL | /health, /projects |
| helidon-rest | 8084 | PostgreSQL | /health, /projects |
| javalin-rest | 8084* | PostgreSQL | /health, /projects |
| armeria-rest | 8085 | PostgreSQL | /health, /projects |
| netty-baseline | 8086 | None | /health |
| activej-rest | 8087 | PostgreSQL | /health, /projects |
| undertow-baseline | 8088 | None | /health |

*Note: Helidon and Javalin share port 8084 - they must be tested sequentially*

## Test Scenarios

### 1. Health Check (Low Concurrency)
- **Purpose**: Measure raw throughput for simple endpoints
- **Configuration**:
  - Threads: 2
  - Connections: 10
  - Duration: 30 seconds

### 2. Health Check (Medium Concurrency)
- **Purpose**: Test scalability under moderate load
- **Configuration**:
  - Threads: 4
  - Connections: 100
  - Duration: 30 seconds

### 3. Health Check (High Concurrency)
- **Purpose**: Stress test maximum throughput
- **Configuration**:
  - Threads: 8
  - Connections: 500
  - Duration: 30 seconds

### 4. Database Query (Low Concurrency)
- **Purpose**: Measure database I/O performance
- **Configuration**:
  - Threads: 2
  - Connections: 10
  - Duration: 30 seconds

### 5. Database Query (Medium Concurrency)
- **Purpose**: Test database connection pooling
- **Configuration**:
  - Threads: 4
  - Connections: 50
  - Duration: 30 seconds

## Scripts

- `run-all-tests.sh` - Master script that runs all tests sequentially
- `test-app.sh` - Individual app testing script
- `analyze-results.sh` - Aggregates and compares results
- `cleanup.sh` - Stops all containers and cleans up
- `results/` - Directory containing test results in JSON and markdown format

## Usage

### Run All Tests
```bash
cd performance-tests
./run-all-tests.sh
```

This will:
1. Test each application sequentially
2. Ensure proper warmup periods
3. Wait for cooling periods between apps
4. Generate comprehensive reports

### Test Individual App
```bash
./test-app.sh <app-name> <port>
# Example:
./test-app.sh light4j-rest-app 8080
```

### Analyze Results
```bash
./analyze-results.sh
```

Generates:
- `results/summary.md` - Markdown comparison table
- `results/summary.json` - Machine-readable results
- `results/charts/` - Performance visualization

## Test Environment Requirements

### System Requirements
- CPU: 4+ cores (8+ recommended)
- RAM: 8GB minimum (16GB recommended)
- Disk: SSD recommended for consistent results

### Software Requirements
- wrk (HTTP benchmarking tool)
- Docker & Docker Compose
- curl (for health checks)
- jq (for JSON processing)

### Pre-Test Checklist
- [ ] Close unnecessary applications
- [ ] Disable CPU throttling
- [ ] Ensure stable network connection
- [ ] No other Docker containers running
- [ ] System not under heavy load

## Test Methodology

### Isolation
Each app is tested in complete isolation:
1. All other containers stopped
2. 30-second warmup period
3. 60-second cooling period between tests
4. System load checked before each test

### Warmup
Before each test scenario:
1. Send 100 requests to warm up JVM
2. Wait 30 seconds for JIT compilation
3. Check health endpoint availability

### Measurement
- Each scenario runs for 30 seconds
- Tests repeated 3 times, median taken
- Outliers (>2σ) are flagged and re-run

### Cooling
Between apps:
1. Container stopped gracefully
2. 60-second wait period
3. Docker cleanup (prune)
4. System memory check

## Results Format

### Per-App Results
Each app gets a dedicated results file:
```
results/
├── light4j-rest-app.json
├── light4j-rest-app.md
├── vertx-lite.json
├── vertx-lite.md
└── ...
```

### Summary Report
Comprehensive comparison:
```
results/
├── summary.md        # Human-readable comparison
├── summary.json      # Machine-readable data
└── TIMESTAMP.log     # Full test execution log
```

## Interpreting Results

### Throughput
- Higher is better
- Measured in requests/second
- Compare across concurrency levels

### Latency
- Lower is better
- Focus on p99 (99th percentile)
- Average can be misleading

### Resource Usage
- Memory footprint during test
- CPU utilization
- Connection pool efficiency

## Troubleshooting

### Port Already in Use
```bash
./cleanup.sh
docker ps -a  # Verify all stopped
```

### Inconsistent Results
- Check system load: `top`, `htop`
- Verify no background processes
- Ensure adequate cooling period
- Check for CPU throttling

### Container Won't Start
```bash
cd ../<app-directory>
docker-compose logs
```

## Notes

- Tests run sequentially to avoid resource contention
- Helidon and Javalin share port 8084 - tested one after another
- Spring-lite is excluded (no HTTP server)
- Baseline apps (netty, undertow) test raw framework performance
- Production apps test full stack including database

## Best Practices

1. **Run tests when system is idle**
2. **Close browsers and IDEs**
3. **Use consistent test environment**
4. **Run multiple times and average**
5. **Document any environmental changes**
6. **Monitor system resources during tests**

## Output Example

```
=== Testing: light4j-rest-app ===
Port: 8080
Starting container...
Waiting for health check... OK
Warming up (30s)...
Running test: Health (Low Concurrency)...
  Throughput: 34,175 req/s
  Latency (avg): 3.38 ms
  Latency (p99): ~21 ms
✓ Test complete

Cooling down (60s)...
```
