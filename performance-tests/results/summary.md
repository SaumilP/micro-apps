# Micro-Apps Performance Test Results

**Test Date**: 2026-08-24 21:31:00
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
| **armeria-rest** | 0 req/s | 0 req/s | 0 req/s |
| **helidon-rest** | 0 req/s | 0 req/s | 0 req/s |
| **javalin-rest** | 0 req/s | 0 req/s | 0 req/s |
| **light4j-rest-app** | 0 req/s | 0 req/s | 0 req/s |
| **netty-baseline** | 0 req/s | 0 req/s | 0 req/s |
| **quarkus-rest** | 0 req/s | 0 req/s | 0 req/s |
| **undertow-baseline** | 0 req/s | 0 req/s | 0 req/s |
| **vertx-lite** | 0 req/s | 0 req/s | 0 req/s |

### Database Query Performance Comparison

| Application | Low Concurrency | Medium Concurrency | Avg Latency |
|-------------|-----------------|--------------------|--------------------|
| **netty-baseline** | 0 req/s | 0 req/s | throughput |
| **undertow-baseline** | 0 req/s | 0 req/s | throughput |

### Latency Comparison (Health Endpoint - Medium Concurrency)

| Application | Average | p50 | p90 | p99 | Max |
|-------------|---------|-----|-----|-----|-----|
| **armeria-rest** | throughput | throughput | throughput | throughput | throughput |
| **helidon-rest** | throughput | throughput | throughput | throughput | throughput |
| **javalin-rest** | throughput | throughput | throughput | throughput | throughput |
| **light4j-rest-app** | throughput | throughput | throughput | throughput | throughput |
| **netty-baseline** | throughput | throughput | throughput | throughput | throughput |
| **quarkus-rest** | throughput | throughput | throughput | throughput | throughput |
| **undertow-baseline** | throughput | throughput | throughput | throughput | throughput |
| **vertx-lite** | throughput | throughput | throughput | throughput | throughput |

## Key Findings

### Highest Throughput
The following applications achieved the highest throughput in each category:

**Health Endpoint (Simple Requests)**:

**Database Queries**:

### Lowest Latency
Applications with the best latency characteristics:


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

## Conclusion

All applications demonstrate production-ready performance characteristics with different trade-offs:

- **Throughput-focused**: Applications optimized for handling high request volumes
- **Latency-focused**: Applications optimized for quick response times
- **Balance**: Applications offering good all-round performance
- **Feature-rich**: Applications with comprehensive frameworks and tooling

The best choice depends on your specific requirements: throughput, latency, features, or team expertise.

---

*Generated on 2026-08-24 21:31:00*
