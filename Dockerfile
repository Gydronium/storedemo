FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} storedemo.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","myspringbootapp-0.0.1-SNAPSHOT.jar"]