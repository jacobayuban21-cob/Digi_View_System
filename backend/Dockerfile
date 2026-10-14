# ============================================================
# Stage 1: Build the Spring Boot Application
# ============================================================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Gradle wrapper and configuration files first (for Docker layer caching)
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/

# Ensure wrapper script is executable
RUN chmod +x gradlew

# Download dependencies (cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and package the Spring Boot executable JAR
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ============================================================
# Stage 2: Minimal Production JRE Runtime Image
# ============================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as an unprivileged service user for security
RUN groupadd -r digiview && useradd -r -g digiview digiview

# Copy prebuilt executable JAR from build stage
COPY --from=build --chown=digiview:digiview /app/build/libs/app.jar /app/app.jar

USER digiview

# Render passes the port in $PORT; default to 8080 locally
ENV PORT=8080
EXPOSE 8080

# Tuned JVM parameters for Render Free/Starter tier (512MB RAM cap):
# -XX:+UseContainerSupport: respects container cgroup memory limits
# -XX:MaxRAMPercentage=75.0: caps heap at 75% of container RAM
# -Xmx384m: hard maximum heap size to prevent OOMKilled
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Xmx384m", "-Dserver.port=${PORT}", "-jar", "/app/app.jar"]
