#FROM openjdk:17-jdk-alpine
#LABEL authors="Thành Trí"
#WORKDIR /app
#COPY build/libs/*.jar app.jar
#EXPOSE 8101
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Stage 1: Build the application
FROM gradle:8.10.2-jdk17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle build files
COPY build.gradle.kts settings.gradle.kts ./

# Copy the source code
COPY src ./src

# Build the application
RUN gradle build -x test --no-daemon

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the Gradle build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8100

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]