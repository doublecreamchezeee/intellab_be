# Stage 1: Use Gradle image to build locally, then copy the build to the runtime image
FROM openjdk:17-jdk AS runtime
#FROM ubuntu:latest
WORKDIR /app

## Use the official Nginx base image
#FROM nginx:latest
#
## Set the environment variable for the timezone
#ENV TZ=Asia/Ho_Chi_Minh
#
## Install the tzdata package to configure the timezone
#RUN apt-get update && \
#    apt-get install -y tzdata && \
#    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
#    echo $TZ > /etc/timezone && \
#    apt-get clean && \
#    rm -rf /var/lib/apt/lists/* \

#FROM ubuntu:20.04
#
#ENV TZ=Asia/Ho_Chi_Minh
#RUN apt-get update && \
#    apt-get install -y tzdata && \
#    ln -fs /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
#    echo $TZ > /etc/timezone && \
#    dpkg-reconfigure -f noninteractive tzdata

# Install tzdata package and set the time zone
#RUN apt-get update && \
#    apt-get install -y tzdata && \
#    ln -fs /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
#    dpkg-reconfigure --frontend noninteractive tzdata

# Set Timezone to Asia/Ho_Chi_Minh
#ENV TZ=Asia/Ho_Chi_Minh
#RUN apt-get update && apt-get install -y tzdata && \
#    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
#    echo $TZ > /etc/timezone && \
#    dpkg-reconfigure -f noninteractive tzdata \
#
##Set timezone
#ENV TZ="Asia/Ho_Chi_Minh"
#
## (Optional) Link timezone files
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime

#FROM alpine:latest
#
#ENV TZ=Asia/Ho_Chi_Minh
#RUN apk add --no-cache tzdata && \
#    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
#    echo $TZ > /etc/timezone

#FROM centos:latest
#
#ENV TZ=Asia/Ho_Chi_Minh
#RUN yum install -y tzdata && \
#    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
#    echo $TZ > /etc/timezone


#Set timezone
ENV TZ="Asia/Ho_Chi_Minh"

# (Optional) Link timezone files
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime

# Copy environment variables (if needed)
COPY deploy.env /app/.env

# Accept port as a build argument, with a default value of 8100
ARG PORT=8101
ENV PORT=$PORT

# Copy the already built JAR file from your local machine
COPY ./build/libs/*.jar app.jar

EXPOSE $PORT

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/,optional:file:config/"]

#date
#cat /etc/timezone