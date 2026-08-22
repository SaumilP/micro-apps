# Multi-stage build for minimal image size
# Stage 1: Build stage
FROM eclipse-temurin:19-jdk-alpine AS builder

# Install Gradle
RUN apk add --no-cache bash wget unzip \
    && wget https://services.gradle.org/distributions/gradle-8.3-bin.zip \
    && unzip gradle-8.3-bin.zip \
    && mv gradle-8.3 /opt/gradle \
    && rm gradle-8.3-bin.zip \
    && ln -s /opt/gradle/bin/gradle /usr/bin/gradle

WORKDIR /build

# Copy all build files
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean assemble --no-daemon

# Stage 2: Runtime stage - minimal JRE for smallest image
FROM eclipse-temurin:19-jre-alpine

# Install minimal dependencies
RUN apk add --no-cache tzdata

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the JAR from builder
COPY --from=builder /build/build/libs/spring-lite-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# No exposed port (this is a demo app, not a web server)
# The application runs and exits after demonstrating DI

# Run the application with optimized JVM settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseSerialGC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
