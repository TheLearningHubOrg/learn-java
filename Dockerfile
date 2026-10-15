# Multi-stage Dockerfile for Modern Java 21+ with Generational ZGC
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean test package -B

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/target/learn-java-1.0.0.jar /app/app.jar

# Run with modern Java 21 Generational ZGC for low-latency GC
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-jar", "/app/app.jar"]
