# Stage 1: Use Gradle image to build locally, then copy the build to the runtime image
FROM openjdk:17-jdk AS runtime

WORKDIR /app

# Copy environment variables (if needed)
COPY docker.env /app/.env

# Accept port as a build argument, with a default value of 8100
ARG PORT=8101
ENV PORT=$PORT

# Copy the already built JAR file from your local machine
COPY ./build/libs/*.jar app.jar

EXPOSE $PORT

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]
