# Framework Selection Decision Guide

This guide will help you choose the right framework based on your specific needs and constraints. We've organized it by common use cases and priorities to make the decision easier.

**Notes**:
- All cost estimates are based on AWS Fargate pricing and should be used as rough guidelines only
- JAR sizes marked with `~` are typical estimates and will vary based on your dependencies
- Performance numbers are from our specific test environment - your results may differ

---

## Quick Decision Tree

```
START HERE: What's your top priority?

├─ Maximum Performance (35k+ req/s)
│  └─ Choose: Undertow or Armeria
│
├─ Cloud Cost Optimization
│  └─ Choose: Micronaut (lowest memory) or Quarkus Native
│
├─ Kubernetes-Native / Cloud-Native
│  └─ Choose: Quarkus (best K8s) or Micronaut (best cloud)
│
├─ Developer Experience / Rapid Development
│  └─ Choose: Javalin (easiest) or Quarkus (live reload)
│
├─ Reactive / High Concurrency (10k+ connections)
│  └─ Choose: Vert.x (best reactive) or Armeria (async)
│
├─ Spring Boot Migration
│  └─ Choose: Micronaut (90% similar) or Quarkus (85% similar)
│
└─ Learning / Education
   └─ Choose: Spring Lite (DI internals) or Netty (async I/O)
```

---

## Decision Matrix

### Rate Your Priorities (1-5)

Use this matrix to score frameworks based on your specific priorities:

| Framework | Performance | Cloud-Native | Learning Curve | Ecosystem | Memory Efficiency | Developer Experience |
|-----------|-------------|--------------|----------------|-----------|-------------------|---------------------|
| **Undertow** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Armeria** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **Light4J** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Micronaut** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Vert.x** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **Quarkus** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Helidon** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **Javalin** | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## By Use Case

### 1. Startup MVP (Limited Budget)

**Requirement**: Launch quickly, keep costs under $50/month, handle 100k-1M requests/day

**Best Choice**: **Micronaut** or **Javalin**

**Why Micronaut**:
- ✅ Lowest cloud cost ($8.25/month on Fargate)
- ✅ Memory efficient (130 MB, 331 req/s/MB)
- ✅ Spring-like API (easy for Java devs)
- ✅ Room to scale to production levels
- ⚠️ Compile-time DI learning curve

**Why Javalin**:
- ✅ Fastest time to market (simple API)
- ✅ Lowest learning curve
- ✅ Great for MVPs and prototypes
- ✅ Easy to understand and maintain
- ⚠️ Lower performance (may need scaling later)

**Estimated Cost**: $8-25/month (varies by region and usage)
**Migration Risk**: Low (both have migration paths)

---

### 2. High-Traffic API (1M+ requests/day)

**Requirement**: Serve 12k req/s peak, 99.9% uptime SLA, cost-efficient scaling

**Best Choice**: **Undertow** or **Armeria**

**Why Undertow**:
- ✅ Single instance handles entire load (37k+ req/s capacity)
- ✅ Lowest latency (379μs avg)
- ✅ $15.50/month for 10k req/s sustained
- ✅ Minimal infrastructure complexity
- ⚠️ Manual observability setup needed

**Why Armeria**:
- ✅ 35k+ req/s throughput
- ✅ Built-in circuit breakers, tracing
- ✅ HTTP/2, gRPC support
- ✅ Production-proven at Line (billions req/day)
- ⚠️ Larger JAR (28 MB), async complexity

**Estimated Cost**: $15-30/month (based on AWS Fargate estimates)
**SLA Achievement**: Should easily meet 99.9% with redundancy

---

### 3. Microservices Platform (50+ services)

**Requirement**: Consistent framework, easy onboarding, cloud-native features, Kubernetes

**Best Choice**: **Quarkus** or **Micronaut**

**Why Quarkus**:
- ✅ Built-in Kubernetes integration
- ✅ Live reload (developer productivity)
- ✅ 1000+ extensions ecosystem
- ✅ Native compilation option (<100ms startup)
- ✅ Red Hat enterprise support available
- ⚠️ Slightly lower raw performance (still excellent 26k req/s)

**Why Micronaut**:
- ✅ Compile-time DI (faster startup, lower memory)
- ✅ Spring-like (easier migration for Java teams)
- ✅ Best memory efficiency (highest pod density)
- ✅ Multi-cloud support (AWS, GCP, Azure SDKs)
- ⚠️ Smaller ecosystem than Quarkus

**Team Onboarding**: 1-2 weeks
**Platform Consistency**: ✅ Excellent (standardized monitoring, deployment)

---

### 4. Real-Time / Event-Driven (100k+ connections)

**Requirement**: 100k+ concurrent WebSocket connections, event-driven processing, pub/sub

**Best Choice**: **Vert.x**

**Why Vert.x**:
- ✅ Event-driven architecture (event bus)
- ✅ Proven at 10k+ concurrent connections
- ✅ Reactive database access (2x performance)
- ✅ Polyglot support (Java, Kotlin, JS)
- ✅ Clustered event bus for distributed systems
- ⚠️ Async complexity, steeper learning curve

**Alternative**: **Armeria** (if RPC needed)

**Expected Performance**: Handle 100k connections with 4-8 GB RAM

---

### 5. Legacy Spring Boot Migration

**Requirement**: Migrate from Spring Boot, reduce costs, improve performance

**Best Choice**: **Micronaut** → **Quarkus** → **Light4J** (in order of similarity)

**Spring Boot → Micronaut**:
- ✅ 90% code similarity
- ✅ 1-2 days migration per service
- ✅ Spring-like annotations (@Controller, @Service)
- ✅ 40% faster, 30% less memory
- ✅ 3-5x cost reduction

**Spring Boot → Quarkus**:
- ✅ 85% code similarity
- ✅ 2-3 days migration per service
- ✅ JAX-RS or Spring-compatible APIs
- ✅ Native compilation option
- ✅ 3-5x cost reduction

**Expected ROI**:
- Cost savings: 60-75%
- Performance improvement: 40-60%
- Migration effort: 1-3 days per service
- Risk: Low (gradual migration possible)

See [Migration Guides](../MIGRATION_GUIDES.md) for detailed code comparisons.

---

### 6. Serverless / FaaS (AWS Lambda, Google Cloud Functions)

**Requirement**: Fast cold start (<1s), low memory, event-driven

**Best Choice**: **Quarkus Native** or **Micronaut**

**Why Quarkus Native**:
- ✅ 15ms startup time (vs 1.5s JVM)
- ✅ 40 MB memory (vs 150 MB JVM)
- ✅ 75% cost reduction on Lambda
- ✅ Perfect for serverless
- ⚠️ 2-5 minute native build time
- ⚠️ Some limitations in native mode

**Why Micronaut**:
- ✅ 1.2s startup (still fast for serverless)
- ✅ 130 MB memory (efficient)
- ✅ Cloud SDK integrations
- ✅ No native compilation issues

**Estimated Lambda Cost Example**:
```
1M invocations, 512 MB memory, 200ms avg duration
(These are rough estimates based on AWS Lambda pricing)

Quarkus Native:  40 MB × 15ms  ≈ $0.83/month
Micronaut:      130 MB × 200ms ≈ $4.17/month
Spring Boot:    200 MB × 1.5s  ≈ $12.50/month

Estimated Savings: ~93% (Quarkus Native vs Spring Boot)
```

---

### 7. Cost-Sensitive / Budget Constrained

**Requirement**: Absolute minimum cloud costs, high efficiency

**Best Choice**: **Micronaut**

**Why Micronaut**:
- ✅ Lowest memory usage (130 MB)
- ✅ Highest efficiency (331 req/s/MB)
- ✅ Highest pod density in Kubernetes
- ✅ $8.25/month for 10k req/s on Fargate
- ✅ 86 pods per 16 GB node

**Estimated Cost Comparison (100M requests/month)**:
```
These are rough estimates based on AWS Fargate pricing

Micronaut:   ~$11.25/month  (Best value)
Undertow:    ~$17.50/month
Quarkus:     ~$19.50/month
Javalin:     ~$39.00/month

Estimated Savings: ~71% (Micronaut vs Javalin)
```

**ROI Calculation**:
- If currently on Spring Boot: **60-70% cost reduction**
- If greenfield: **Start optimized from day one**

---

### 8. Enterprise / Compliance (Financial Services, Healthcare)

**Requirement**: Production-proven, enterprise support, compliance, stability

**Best Choice**: **Light4J**, **Undertow**, or **Quarkus**

**Why Light4J**:
- ✅ Proven in financial services
- ✅ Built-in security (OAuth2, JWT)
- ✅ Excellent performance (29k req/s)
- ✅ Production stability focus
- ⚠️ Smaller ecosystem

**Why Undertow**:
- ✅ Powers WildFly (Red Hat)
- ✅ Used by Fortune 500 companies
- ✅ Maximum performance
- ✅ Proven production stability

**Why Quarkus**:
- ✅ Red Hat enterprise support
- ✅ Cloud-native, Kubernetes-native
- ✅ Large ecosystem (1000+ extensions)
- ✅ Compliance-friendly (SOC2, HIPAA compatible)

**Compliance**: All three support audit logging, security frameworks, enterprise requirements

---

### 9. Developer Productivity / Rapid Iteration

**Requirement**: Fast development cycles, easy debugging, quick onboarding

**Best Choice**: **Javalin** or **Quarkus**

**Why Javalin**:
- ✅ Simplest API (learn in 1 hour)
- ✅ Lowest learning curve
- ✅ Great documentation
- ✅ Easy debugging
- ✅ Small JAR (9.2 MB)
- ⚠️ Lower performance (still good 19k req/s)

**Why Quarkus**:
- ✅ Live reload in dev mode ⭐
- ✅ Code changes without restart
- ✅ Extensive extensions (auth, DB, messaging)
- ✅ Developer-friendly error messages
- ✅ Good performance (26k req/s)

**Time to First Feature**:
- Javalin: 30 minutes
- Quarkus: 1 hour
- Micronaut: 2 hours
- Vert.x: 4 hours (async complexity)

---

### 10. Learning / Educational

**Requirement**: Understand framework internals, DI, async I/O

**Best Choice by Learning Goal**:

**Spring-Like DI Internals**: **Spring Lite**
- ✅ Custom DI container from scratch
- ✅ Zero dependencies
- ✅ Understand how Spring works
- ✅ Great for interviews

**Async I/O Fundamentals**: **Netty Baseline**
- ✅ Pure Netty event loops
- ✅ Understand async architecture
- ✅ Foundation of many frameworks

**Production Web Framework**: **Javalin**
- ✅ Simple, understandable codebase
- ✅ Learn web framework basics
- ✅ Actually useful for projects

---

## Comparison Tables

### Performance vs Features Trade-off

| Framework | Performance | Features | Complexity | Best For |
|-----------|-------------|----------|------------|----------|
| **Undertow** | ⭐⭐⭐⭐⭐ | ⭐⭐ | Low | Maximum performance |
| **Armeria** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Medium | High-perf + features |
| **Micronaut** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Medium | Cloud cost optimization |
| **Quarkus** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Medium | Kubernetes platforms |
| **Javalin** | ⭐⭐⭐ | ⭐⭐⭐ | Low | Rapid development |

### Cloud Cost Ranking

**Note**: All costs are estimates based on AWS Fargate pricing and actual costs will vary.

| Rank | Framework | Estimated Monthly Cost (10k req/s) | Estimated Cost per 1M Requests |
|------|-----------|-------------------------------------|-------------------------------|
| 1 | **Micronaut** | ~$8.25 | ~$0.03 |
| 2 | **Undertow** | ~$15.50 | ~$0.02 |
| 2 | **Armeria** | ~$15.50 | ~$0.02 |
| 4 | **Light4J** | ~$15.50 | ~$0.03 |
| 5 | **Quarkus** | ~$15.50 | ~$0.04 |
| 6 | **Javalin** | ~$31.00 | ~$0.08 |

### Startup Time Ranking (Cold Start)

| Rank | Framework | Startup Time | Best For |
|------|-----------|--------------|----------|
| 1 | **Quarkus Native** | <100ms ⭐ | Serverless |
| 2 | **Micronaut** | 1.2s | Auto-scaling |
| 3 | **Light4J** | 1.8s | Standard deployment |
| 4 | **Undertow** | 2.0s | Standard deployment |
| 5 | **Vert.x** | 2.1s | Reactive apps |
| 6 | **Helidon** | 2.5s | Standard deployment |

### Ecosystem & Community

| Framework | Community Size | Extensions/Plugins | Enterprise Support | Maturity |
|-----------|----------------|-------------------|-------------------|----------|
| **Quarkus** | ⭐⭐⭐⭐⭐ | 1000+ | Red Hat | ⭐⭐⭐⭐ |
| **Micronaut** | ⭐⭐⭐⭐ | 100+ | Object Computing | ⭐⭐⭐⭐ |
| **Vert.x** | ⭐⭐⭐⭐ | 50+ | Eclipse Foundation | ⭐⭐⭐⭐⭐ |
| **Undertow** | ⭐⭐⭐ | Moderate | Red Hat (WildFly) | ⭐⭐⭐⭐⭐ |
| **Armeria** | ⭐⭐⭐ | Moderate | Line Corporation | ⭐⭐⭐⭐ |
| **Javalin** | ⭐⭐⭐ | Moderate | Community | ⭐⭐⭐ |
| **Light4J** | ⭐⭐ | Limited | NetworkNT | ⭐⭐⭐⭐ |
| **Helidon** | ⭐⭐ | Moderate | Oracle | ⭐⭐⭐ |

---

## Risk Assessment

### Migration Risk Matrix

| From → To | Risk Level | Effort | Code Similarity | Timeline |
|-----------|------------|--------|----------------|----------|
| Spring Boot → Micronaut | Low | Low | 90% | 1-2 days/service |
| Spring Boot → Quarkus | Low | Medium | 85% | 2-3 days/service |
| Express.js → Javalin | Medium | Medium | 60% | 1-2 weeks |
| Any → Vert.x | High | High | 30% | 2-4 weeks |

### Production Readiness

| Framework | Battle-Tested | Production Features | Documentation | Risk |
|-----------|---------------|-------------------|---------------|------|
| **Undertow** | ✅ Fortune 500 | ⚠️ Manual setup | ✅ Good | Low |
| **Armeria** | ✅ Line (billions req/day) | ✅ Built-in | ✅ Good | Low |
| **Light4J** | ✅ Financial services | ✅ Security built-in | ⚠️ Limited | Low-Med |
| **Micronaut** | ✅ Enterprises | ✅ Cloud-native | ✅ Excellent | Low |
| **Quarkus** | ✅ Red Hat customers | ✅ Extensive | ✅ Excellent | Low |
| **Vert.x** | ✅ Large scale | ⚠️ Manual setup | ✅ Good | Medium |
| **Javalin** | ⚠️ Small-medium | ⚠️ Limited | ✅ Excellent | Medium |

---

## Decision Scorecard

### How to Use

1. Rate each factor's importance (1-5)
2. Multiply framework score by importance
3. Sum totals for each framework
4. Highest score = best fit

**Example**:

| Factor | Importance | Undertow | Micronaut | Quarkus | Javalin |
|--------|------------|----------|-----------|---------|---------|
| Performance | 5 | 5×5=25 | 4×5=20 | 4×5=20 | 3×5=15 |
| Cloud Cost | 4 | 4×4=16 | 5×4=20 | 4×4=16 | 2×4=8 |
| Dev Speed | 2 | 3×2=6 | 4×2=8 | 5×2=10 | 5×2=10 |
| Ecosystem | 3 | 3×3=9 | 4×3=12 | 5×3=15 | 3×3=9 |
| **TOTAL** | - | **56** | **60** | **61** | **42** |

**Result**: Quarkus wins for this specific priority set.

---

## Common Mistakes to Avoid

### ❌ Choosing Based on Hype

**Mistake**: "Everyone uses Spring Boot, so we should too"
**Better**: Evaluate based on actual requirements (performance, cost, team skills)

### ❌ Ignoring Total Cost of Ownership

**Mistake**: "This framework is free, so it's cheaper"
**Better**: Calculate cloud costs, developer time, maintenance effort

### ❌ Over-Engineering

**Mistake**: "We might need 100k req/s someday, so let's use Undertow"
**Better**: If you need 1k req/s now, Javalin is fine. Migrate if needed later.

### ❌ Under-Engineering

**Mistake**: "Javalin is simple, let's use it for everything"
**Better**: For 50+ microservices platform, use Quarkus/Micronaut for consistency.

### ❌ Ignoring Team Skills

**Mistake**: "Vert.x is fastest for our use case, let's use it"
**Better**: If team doesn't know async, learning curve costs > performance gains.

---

## Final Recommendations

### By Team Experience

**Java Beginners**: Javalin → Micronaut → Quarkus
**Spring Boot Developers**: Micronaut or Quarkus
**Node.js Background**: Javalin or Vert.x (async familiar)
**Performance Engineers**: Undertow or Armeria
**Cloud/DevOps Focus**: Quarkus or Micronaut

### By Company Stage

**Pre-Product**: Javalin (MVP fast)
**Early Startup**: Micronaut (cost + growth)
**Growth Stage**: Quarkus or Micronaut (scale + features)
**Enterprise**: Quarkus, Light4J, or Undertow (stability)

### Safe Default Choices

If unsure, these are safe bets:

1. **Quarkus** - Best all-around for cloud-native
2. **Micronaut** - Best for cost optimization
3. **Javalin** - Best for rapid development

---

## Next Steps

After choosing your framework:

1. **Read Framework Details**: [Frameworks Guide](FRAMEWORKS.md)
2. **Check Performance**: [Benchmarks](BENCHMARKS.md)
3. **Get Started**: [Getting Started Guide](GETTING_STARTED.md)
4. **Plan Migration**: [Migration Guides](../MIGRATION_GUIDES.md) (if applicable)
5. **Deploy**: [Deployment Guide](DEPLOYMENT.md)

---

**Need help deciding? Open a [GitHub Discussion](https://github.com/your-org/micro-apps/discussions) with your requirements!**

---

**Last Updated**: August 25, 2026
