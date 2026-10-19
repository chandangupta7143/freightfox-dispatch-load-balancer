# Multi-stage Docker build for FreightFox Dispatch Load Balancer
# Base build image: Java 21 + Maven 3.9
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy POM and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production JAR
COPY src src
RUN mvn clean package -DskipTests

# Runtime image: Lightweight Eclipse Temurin 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled JAR from builder stage
COPY --from=builder /app/target/freightfox-dispatch-load-balancer-*.jar app.jar

# Dynamic port binding (Railway injects PORT env variable)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
