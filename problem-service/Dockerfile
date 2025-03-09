FROM openjdk:17-jdk AS runtime

WORKDIR /app

# Copy environment variables (if needed)
COPY docker.env /app/.env

# Accept port as a build argument, with a default value of 8100
ARG PORT=8103
ENV PORT=$PORT

# Copy the already built JAR file from your local machine
COPY ./build/libs/*.jar app.jar

# Create the problems directory and copy its contents if it exists
RUN mkdir -p /app/problems && \
    if [ -d "./src/main/resources/problems/" ]; then \
    cp -r ./src/main/resources/problems/* /app/problems/; \
    fi

EXPOSE $PORT

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]