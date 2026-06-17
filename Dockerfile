# === STAGE 1: COMPILE THE APPLICATION IN THE CLOUD ===
# FIXED: Replaced non-existent Maven image with a stable, active JDK 25 compiler
FROM amazoncorretto:25 AS build
WORKDIR /build

# Copy Maven wrapper configurations and source files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Ensure the build wrapper has execution privileges
RUN chmod +x mvnw

# Compile and package the application artifact safely using the wrapper
RUN ./mvnw clean package -DskipTests

# === STAGE 2: RUN THE APPLICATION RUNTIME ===
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

# Create data directory for H2 database persistence
RUN mkdir -p /app/data && chmod 777 /app/data

# Copy the compiled JAR straight from Stage 1
COPY --from=build /build/target/*.jar app.jar

# Setup security privileges
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=40.0", "-jar", "app.jar"]
