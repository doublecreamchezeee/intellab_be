FROM debian:bookworm-slim

# install OpenJDK 17
RUN apt-get update && apt-get install -y openjdk-17-jre-headless

WORKDIR /app

COPY docker.env /app/.env

ARG PORT=8103
ENV PORT=$PORT

COPY ./build/libs/*.jar app.jar

RUN mkdir -p /app/problems

EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]
