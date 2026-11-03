# Multi-stage build for the catalog service (plain JDK, no framework).
# Stage 1 compiles and tests with the full JDK; stage 2 ships only the JRE and app.jar.

# ---- build ------------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
# Wrapper and build scripts first: this layer (Gradle download + dependency
# resolution) stays cached until one of these files changes.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src ./src
RUN ./gradlew --no-daemon jar -x test && cp build/libs/app.jar /src/app.jar

# ---- runtime ----------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime
RUN useradd --uid 10001 --no-create-home --shell /usr/sbin/nologin app
WORKDIR /app
COPY --from=build /src/app.jar app.jar
USER app
# The process reads PORT and binds 0.0.0.0. Keep PORT, EXPOSE and APP_PORT in ci.yml equal.
ENV PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
