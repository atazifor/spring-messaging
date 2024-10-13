# Stage 1: Build the application using Gradle
FROM gradle:7.5.0-jdk17 AS builder

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper files if available (this helps with dependency caching)
COPY gradlew gradlew
COPY gradle gradle

# Copy the Gradle project files
COPY build.gradle settings.gradle /app/

# Pre-cache the dependencies (this allows Docker to cache these steps)
RUN gradle clean build -x test --no-daemon || return 0

# Copy the rest of the application source
COPY . .

# Build the application (including the jar)
RUN gradle build -x test --no-daemon

# Stage 2: Create the image to run the application
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage to this image
COPY --from=builder /app/build/libs/*.jar app.jar

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
