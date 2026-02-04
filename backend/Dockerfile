# Build stage — use Gradle directly (no wrapper jar in repo)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install Gradle 8.5 (avoids missing gradle-wrapper.jar in COPY)
RUN apk add --no-cache unzip && \
    wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O gradle.zip && \
    unzip -q gradle.zip && mv gradle-8.5 gradle && rm gradle.zip && \
    apk del unzip
ENV PATH="/app/gradle/bin:${PATH}"

COPY build.gradle.kts settings.gradle.kts .
COPY buildSrc buildSrc
COPY backend backend
COPY shared shared

RUN gradle :backend:installDist --no-daemon --no-build-cache

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/backend/build/install/backend .

EXPOSE 8080
ENV PORT=8080
CMD ["./bin/backend"]
