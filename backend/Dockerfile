# Build stage — only backend (no shared/KMP to avoid Kotlin/Native in Linux)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install Gradle 8.5
RUN apk add --no-cache unzip && \
    wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O gradle.zip && \
    unzip -q gradle.zip && mv gradle-8.5 gradle && rm gradle.zip && \
    apk del unzip
ENV PATH="/app/gradle/bin:${PATH}"

# Use Docker settings (backend only); copy only what backend needs
COPY build.gradle.kts .
COPY settings-docker.gradle.kts settings.gradle.kts
COPY buildSrc buildSrc
COPY backend backend

# More memory for Gradle in container
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -Xmx512m"

RUN gradle :backend:installDist --no-daemon --no-build-cache --stacktrace

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/backend/build/install/backend .

EXPOSE 8080
ENV PORT=8080
CMD ["./bin/backend"]
