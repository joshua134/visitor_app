#FROM maven:3.9.6-eclipse-temurin-25-alpine AS build
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /build

# Copy build config and source code
COPY pom.xml .
COPY src ./src

# Compile and package the application artifact safely
RUN mvn clean package -DskipTests

# === STAGE 2: RUN THE APPLICATION RUNTIME ===
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

# Create data directory for H2 database persistence
RUN mkdir -p /app/data && chmod 777 /app/data

# Copy the compiled JAR straight from Stage 1 (Bypasses local target folder issues)
COPY --from=build /build/target/*.jar app.jar

# Setup security privileges
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=40.0", "-jar", "app.jar"]

#FROM eclipse-temurin:25-jdk-alpine

# Set up working directory
#WORKDIR /app

# Create data directory for H2 database files
#RUN mkdir -p /app/data && chmod 777 /app/data

# Copy jar file
#COPY target/*.jar app.jar

# Create a non-root user for security
#RUN addgroup -S appgroup && adduser -S appuser -G appgroup
#USER appuser

#EXPOSE 8080

# Use H2 profile by default
#ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=40.0", "-jar", "app.jar"]
