# Dockerfile for the Spring Boot Application
FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY . .
RUN mvn -Daether.remoteRepositoryFilter.prefixes=true -Daether.remoteRepositoryFilter.prefixes.basedir=/build/.mvn/rrf -DskipTests clean package


# glibc-based image: ONNX Runtime (used via Magika) ships native libraries that need glibc and libstdc++
FROM eclipse-temurin:25-jre
COPY --from=build /build/target/rest-*.jar /app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT java -jar /app.jar --server.use-forward-headers=true --server.forward-headers-strategy=framework
