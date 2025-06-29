# Use Debian Slim for small size
FROM debian:bullseye-slim

# Install Perl
RUN apt-get update && apt-get install -y perl \
    && rm -rf /var/lib/apt/lists/*

# Create working directory
WORKDIR /app

# Copy moss.pl into container
COPY moss.pl /app/moss.pl

# Make sure it's executable
RUN chmod +x /app/moss.pl

# Default entrypoint
ENTRYPOINT ["perl", "/app/moss.pl"]
