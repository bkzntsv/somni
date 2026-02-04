# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Gradle wrapper and build config
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts .
COPY buildSrc buildSrc
COPY backend backend
COPY shared shared

RUN chmod +x gradlew && ./gradlew :backend:installDist --no-daemon --no-build-cache

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/backend/build/install/backend .

EXPOSE 8080
ENV PORT=8080
CMD ["./bin/backend"]
