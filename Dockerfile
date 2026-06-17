FROM eclipse-temurin:25-jdk-alpine

# Set up working directory
WORKDIR /app

# Create data directory for H2 database files
RUN mkdir -p /app/data && chmod 777 /app/data

# Copy jar file
COPY target/*.jar app.jar

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

# Use H2 profile by default
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=40.0", "-jar", "app.jar"]
