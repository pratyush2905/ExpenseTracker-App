FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY app/build/libs/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]