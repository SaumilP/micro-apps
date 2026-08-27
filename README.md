# ⚡ Java Microservices Performance Benchmark

<div align="center">

**Comprehensive performance comparison of 11 Java microservice frameworks**

[![Frameworks](https://img.shields.io/badge/frameworks-11-blue?style=for-the-badge)](docs/FRAMEWORKS.md)
[![Tests](https://img.shields.io/badge/tests-90+-green?style=for-the-badge)](docs/BENCHMARKS.md)
[![Max Throughput](https://img.shields.io/badge/max_throughput-37.8k_req/s-brightgreen?style=for-the-badge)](docs/BENCHMARKS.md)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](LICENSE)

[Quick Start](#-quick-start) • [Benchmarks](docs/BENCHMARKS.md) • [Frameworks](docs/FRAMEWORKS.md) • [Documentation](#-documentation)

</div>

---

## 🎯 What Is This?

A **production-ready benchmark suite** comparing 11 Java microservice frameworks under identical conditions. Get **real performance data** to make informed framework decisions.

**Not just benchmarks** - includes production Kubernetes configs, complete observability stack, and migration guides.

## 📊 Performance At a Glance

```
Throughput Comparison (requests/second @ 100 concurrent connections)

Undertow     ████████████████████████████████████████ 37,808 req/s ⚡ Fastest
Armeria      ██████████████████████████████████████   35,810 req/s
Light4J      ████████████████████████████████         29,583 req/s
Micronaut    ███████████████████████████████          28,130 req/s 💾 Most Efficient
Vert.x       ███████████████████████████████          28,073 req/s
Quarkus      ██████████████████████████               26,608 req/s ☁️ Best Cloud-Native
ActiveJ      ██████████████████████████               25,429 req/s 📦 Smallest JAR
Helidon      ████████████████████████                 23,340 req/s
Javalin      ████████████████████                     19,853 req/s 🎨 Best DX
Netty        ██████████████████                       18,159 req/s
```

<details>
<summary><b>📈 View Detailed Metrics</b></summary>

| Framework | Throughput | Latency (p50) | Memory | JAR Size | Best For |
|-----------|------------|---------------|--------|----------|----------|
| **Undertow** | 37,808 req/s | 379μs | 180 MB | 7.1 MB | Maximum performance |
| **Armeria** | 35,810 req/s | 435μs | 210 MB | 28 MB | Async RPC/REST |
| **Light4J** | 29,583 req/s | 514μs | 165 MB | 12 MB | Production APIs |
| **Micronaut** | 28,130 req/s | 675μs | 130 MB | TBD | Cloud-native, cost |
| **Vert.x** | 28,073 req/s | 603μs | 240 MB | TBD | Reactive systems |
| **Quarkus** | 26,608 req/s | 736μs | 140 MB | TBD | Kubernetes-native |
| **ActiveJ** | 25,429 req/s | 19.56ms | ~170 MB | 5.3 MB | Minimal footprint |

**[→ Full Benchmark Results](docs/BENCHMARKS.md)** with charts, latency distributions, and cost analysis.

</details>

## ✨ Key Highlights

| 🚀 **Performance** | 💾 **Efficiency** | ☁️ **Production Ready** | 🔄 **Migration** |
|-------------------|-------------------|------------------------|------------------|
| **37k+ req/s** peak throughput | **331 req/s/MB** (Micronaut) | Full K8s manifests | Spring Boot → Micronaut in **1-2 days** |
| Sub-millisecond latency | **40 MB** memory (Quarkus native) | Prometheus + Grafana stack | Code migration guides |
| Real benchmark data | **75%** cloud cost savings | Auto-scaling configs | **90%** code similarity |

## 🚀 Quick Start

Get running in under 2 minutes:

```bash
# Clone repository
git clone https://github.com/your-org/java-framework-showdown.git
cd java-framework-showdown

# Run fastest framework (Undertow)
cd undertow-baseline
docker compose up -d

# Test it
curl http://localhost:8088/health
# Response: {"status":"UP"}

# Run benchmark
cd ../performance-tests
./test-app.sh undertow-baseline 8088
```

**[→ Detailed Getting Started Guide](docs/GETTING_STARTED.md)** for first-time setup, prerequisites, and tutorials.

## 🎯 Find Your Path

Choose your journey based on your goal:

<table>
<tr>
<td width="50%">

### 👨‍💻 **I'm a Developer**
Starting development or exploring frameworks

**Quick Links:**
- 🚀 [Getting Started (5 min)](docs/GETTING_STARTED.md)
- 📦 [All 11 Frameworks](docs/FRAMEWORKS.md)
- 💻 [Code Examples](docs/CODE_EXAMPLES.md)
- 🧪 [Run Tests](performance-tests/README.md)

</td>
<td width="50%">

### 🏢 **I'm Making a Decision**
Choosing framework for new project

**Quick Links:**
- 📊 [Performance Benchmarks](docs/BENCHMARKS.md)
- 🎯 [Decision Guide](docs/DECISION_GUIDE.md)
- 💰 [Cost Analysis](docs/BENCHMARKS.md#cost-analysis)
- ⚖️ [Framework Comparison](docs/FRAMEWORKS.md#comparison-matrix)

</td>
</tr>
<tr>
<td width="50%">

### ☁️ **I'm Deploying to Production**
Need production deployment configs

**Quick Links:**
- 🐳 [Docker Deployment](docs/DEPLOYMENT.md)
- ☸️ [Kubernetes Manifests](k8s/)
- 📊 [Observability Stack](observability/)
- 🔐 [Security Best Practices](docs/DEPLOYMENT.md#security)

</td>
<td width="50%">

### 🔄 **I'm Migrating Frameworks**
Moving from existing framework

**Quick Links:**
- 🔀 [Migration Guides](MIGRATION_GUIDES.md)
- 🍃 [Spring Boot → Micronaut](MIGRATION_GUIDES.md#spring-boot--micronaut)
- ⚡ [Spring Boot → Quarkus](MIGRATION_GUIDES.md#spring-boot--quarkus)
- 📈 [Expected ROI](MIGRATION_GUIDES.md#roi-analysis)

</td>
</tr>
</table>

## 📚 Complete Documentation

### **Core Guides** (Start Here)
| Guide | Description | Read Time |
|-------|-------------|-----------|
| **[📦 Frameworks](docs/FRAMEWORKS.md)** | Detailed info on all 11 frameworks | 10 min |
| **[📊 Benchmarks](docs/BENCHMARKS.md)** | Performance results with charts | 8 min |
| **[🚀 Getting Started](docs/GETTING_STARTED.md)** | First-time setup & tutorials | 5 min |
| **[🎯 Decision Guide](docs/DECISION_GUIDE.md)** | Choose the right framework | 7 min |

### **Advanced Topics**
| Guide | Description | Read Time |
|-------|-------------|-----------|
| **[🚄 GraalVM Native](GRAALVM_NATIVE.md)** | Native compilation (100x faster startup) | 12 min |
| **[💾 Memory Analysis](MEMORY_ANALYSIS.md)** | Deep dive into memory usage | 15 min |
| **[🔄 Migration Guides](MIGRATION_GUIDES.md)** | Step-by-step framework migration | 10 min |
| **[☁️ Deployment](docs/DEPLOYMENT.md)** | Docker, K8s, cloud platforms | 10 min |

### **Production Resources**
| Resource | Description |
|----------|-------------|
| **[☸️ Kubernetes](k8s/)** | Production-ready K8s manifests with HPA, monitoring |
| **[📊 Observability](observability/)** | Prometheus, Grafana, Jaeger, Loki stack |
| **[🧪 Performance Tests](performance-tests/)** | Benchmark methodology & scripts |
| **[🔬 Detailed Analysis](PERFORMANCE_COMPARISON.md)** | Comprehensive performance comparison |

## 🏆 Framework Quick Picks

Not sure where to start? Here are our top picks for common scenarios:

| Scenario | Recommendation | Why |
|----------|---------------|-----|
| **🚀 Maximum Performance** | **Undertow** or **Armeria** | 35k+ req/s, sub-millisecond latency |
| **💰 Cost Optimization** | **Micronaut** or **Quarkus** | 30-50% lower cloud costs, efficient memory |
| **☁️ Cloud-Native/K8s** | **Quarkus** or **Micronaut** | Built-in K8s support, fast startup |
| **🔄 Reactive/High Concurrency** | **Vert.x** or **Armeria** | Event-driven, handles 10k+ connections |
| **🎨 Developer Experience** | **Javalin** | Simple API, low learning curve |
| **🍃 Spring Boot Alternative** | **Micronaut** | 90% similar, 40% faster, 30% less memory |
| **⚡ Serverless/FaaS** | **Quarkus Native** | 15ms startup, 40 MB memory |

**[→ Full Decision Guide](docs/DECISION_GUIDE.md)** with detailed comparison and use cases.

## 🌟 What Makes This Different?

Unlike toy benchmarks, this project provides:

✅ **Real-world conditions**: All tests on identical hardware, same workload <br />
✅ **Production configs**: K8s manifests, monitoring, scaling configs <br />
✅ **Complete stack**: Not just code - includes deployment, observability, migration <br />
✅ **Maintained**: Regular updates with latest framework versions <br />
✅ **Transparent**: Full methodology documented, reproducible results <br/>
✅ **Practical**: Focused on metrics that matter (cost, latency, throughput)

## 🤝 Contributing

We welcome contributions! Here's how you can help:

- 🐛 **Report Issues**: Found a bug or inaccuracy? [Open an issue](https://github.com/SaumilP/java-framework-showdown/issues)
- 📊 **Add Benchmarks**: Have test results from different hardware? Share them!
- 🔧 **Improve Code**: Optimize implementations, fix bugs
- 📖 **Enhance Docs**: Improve guides, add examples
- ⭐ **Star the Repo**: Show your support and help others discover this project

**[→ Contributing Guide](CONTRIBUTING.md)** for detailed guidelines.

## 📈 Project Stats

- **11 Java Frameworks** tested
- **90+ Test Scenarios** across all frameworks
- **6 Comprehensive Guides** (40k+ words)
- **Production K8s Configs** for top frameworks
- **Complete Observability** stack included
- **Last Updated**: August 24, 2026
- **Test Date**: August 23, 2026

## 📞 Support & Community

- 💬 **Discussions**: [GitHub Discussions](https://github.com/SaumilP/java-framework-showdown/discussions)
- 🐛 **Issues**: [Report bugs or request features](https://github.com/SaumilP/java-framework-showdown/issues)
- 📧 **Contact**: For business inquiries or collaborations

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Individual frameworks have their own licenses. Check each framework directory for details.

---

<div align="center">

**⭐ Star this repo if it helped you make a better framework decision!**

Built with ❤️ by the community • [Report Issues](https://github.com/your-org/java-framework-showdown/issues) • [Contribute](CONTRIBUTING.md)

</div>
