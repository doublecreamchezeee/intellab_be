# Stage 1: Use Docker Compose to build services
FROM docker:latest as build

WORKDIR /app

# Copy the Docker Compose file to the container
COPY docker-compose.yml .

# Stage 2: Copy the built images into the container (optional)
# If needed, copy each of the pre-built service images, or you can just rely on them being pulled from Docker Hub.

# Example of copying the built images if they exist locally
COPY api-gateway-image:latest /api-gateway-image
COPY identity-service-image:latest /identity-service-image
COPY course-service-image:latest /course-service-image

# Expose a default port if needed for the root service
EXPOSE 8765

# Default command (to run docker-compose from within the image)
CMD ["docker-compose", "up"]
