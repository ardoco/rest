# Dockerfile for the Spring Boot Application

FROM alpine/java:21-jdk

ARG JAR_FILE=target/parent-rest-1.0.0-SNAPSHOT.jar
COPY ./target/parent-rest-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]