# Multi-stage Docker build for FreightFox Dispatch Load Balancer
# Base build image: Java 21 + Maven 3.9 on Alpine
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy POM and download dependencies for efficient layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production JAR
COPY src src
RUN mvn clean package -DskipTests

# Runtime image: Lightweight Eclipse Temurin 21 JRE on Alpine
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-privileged user and group for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /app/target/freightfox-dispatch-load-balancer-*.jar app.jar

# Set ownership to non-root user
RUN chown appuser:appgroup app.jar

# Drop privileges to non-root user
USER appuser:appgroup

# Dynamic port binding: Render injects PORT env variable at runtime (defaults to 8080 locally)
ENV PORT=8080
EXPOSE 8080

# Production JVM configuration:
# - UseContainerSupport: respects container memory cgroups
# - MaxRAMPercentage=75.0: caps heap at ~384 MB on 512 MB instances to prevent OOM kills
# - java.security.egd: non-blocking entropy source for fast startup
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
