FROM openjdk:17-jdk AS runtime

WORKDIR /app

# Copy environment variables (if needed)
COPY docker.env /app/.env

# Accept port as a build argument, with a default value of 8100
ARG PORT=8103
ENV PORT=$PORT

# Copy the already built JAR file from your local machine
COPY ./build/libs/*.jar app.jar

RUN mkdir -p /app/problems
# Adjust the path to correctly reference the problems directory
COPY ./src/main/resources/problems/ /app/problems

EXPOSE $PORT

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]