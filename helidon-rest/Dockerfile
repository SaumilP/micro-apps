FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

WORKDIR /app

COPY --from=builder /build/target/helidon-rest-1.0.0.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

ENV SERVER_PORT=8084 \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=projectdb \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Xlog:gc:stdout:time"

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
