# Dockerfile for the Spring Boot Application
FROM maven:3-eclipse-temurin-24-alpine AS build
WORKDIR /build
COPY . .
RUN mvn -DskipTests clean package


FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache bash
COPY --from=build /build/target/rest-*.jar /app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT java -jar /app.jar --server.use-forward-headers=true --server.forward-headers-strategy=framework
