FROM openjdk:17-jdk-alpine
LABEL authors="Thành Trí"
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8101
ENTRYPOINT ["java", "-jar", "app.jar"]