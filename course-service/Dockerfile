## Stage 1: Build the application
#FROM gradle:8.10.2-jdk17 AS build
#
#WORKDIR /app
#COPY build.gradle.kts settings.gradle.kts ./
#COPY src ./src
#
#RUN gradle build -x test --no-daemon
#
## Stage 2: Create the runtime image
#FROM openjdk:17-jdk-slim
#
#WORKDIR /app
#
#COPY docker.env /app/.env
#
## Accept port as build argument
#ENV PORT=$PORT
#
## Copy built JAR file
#COPY --from=build /app/build/libs/*.jar app.jar
#
#EXPOSE $PORT
#
#ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]

# Stage 1: Use Gradle image to build locally, then copy the build to the runtime image
FROM openjdk:17-jdk-slim AS runtime

WORKDIR /app

# Copy environment variables (if needed)
COPY docker.env /app/.env

# Accept port as a build argument, with a default value of 8100
ARG PORT=8102
ENV PORT=$PORT

# Copy the already built JAR file from your local machine
COPY ./build/libs/*.jar app.jar

EXPOSE $PORT

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]
