FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN mkdir -p /app/data && chmod 777 /app/data

COPY --from=build /build/target/*.jar app.jar

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

USER appuser

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]