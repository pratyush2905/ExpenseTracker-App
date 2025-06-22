FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/userService-0.0.1-SNAPSHOT.jar userService-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/app/userService-0.0.1-SNAPSHOT.jar"]