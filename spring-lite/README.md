# Spring Lite - Minimal DI Framework

An educational implementation of a Spring-like dependency injection framework with **zero external dependencies**. Built to demonstrate the core concepts of DI containers, component scanning, and autowiring.

## Features

- **Zero Dependencies**: Pure Java implementation
- **Dependency Injection**: Constructor and field injection
- **Component Scanning**: Annotation-based discovery
- **Request Mapping**: HTTP route simulation
- **Autowiring**: Automatic dependency resolution
- **Minimal Size**: 9.5 KB JAR

## What It Does

This is NOT a production web framework. It's an educational tool that:

1. Scans for classes annotated with `@Component`
2. Builds a dependency injection container
3. Autowires dependencies automatically
4. Simulates HTTP request handling
5. Demonstrates DI concepts in action

## Quick Start

### Using Docker

```bash
# Build image
docker build -t spring-lite .

# Run demo
docker run --rm spring-lite

# Output shows DI container in action
```

### Local Development

Requirements:
- Java 19+
- Gradle 8+

```bash
# Build
gradle clean assemble

# Run
java -jar build/libs/spring-lite-1.0.0.jar
```

## Performance Metrics

- **JAR Size**: 9.5 KB
- **Docker Image**: 179 MB (mostly JRE)
- **Startup Time**: < 100ms
- **Memory Usage**: ~50-80 MB
- **Dependencies**: 0

See [PERFORMANCE.md](PERFORMANCE.md) for detailed analysis.

## Project Structure

```
spring-lite/
├── src/main/java/org/sandcastle/apps/
│   ├── Application.java              # Entry point
│   ├── framework/                    # Framework code
│   │   ├── LiteServer.java          # Main server with DI
│   │   ├── Container.java           # DI container
│   │   ├── HttpRequest.java         # Request abstraction
│   │   ├── HttpResponse.java        # Response abstraction
│   │   └── annotations/
│   │       ├── Component.java       # @Component
│   │       ├── Autowired.java       # @Autowired
│   │       └── RequestMapping.java  # @RequestMapping
│   └── web/                          # Application code
│       ├── SimpleController.java    # Demo controller
│       └── HiService.java           # Demo service
└── build.gradle                      # Build config
```

## Example Usage

```java
@Component
public class SimpleController {
    @Autowired
    private HiService service;

    @RequestMapping("/hi")
    public String handleHi(HttpRequest req) {
        return service.sayHi();
    }
}
```

## Educational Value

Understanding Spring Lite helps you understand:
- How Spring's ApplicationContext works
- How component scanning is implemented
- How dependency injection resolves dependencies
- How annotations drive configuration

Perfect for students, developers learning framework internals, and teaching Spring concepts.

## License

MIT License - See root repository for details.
