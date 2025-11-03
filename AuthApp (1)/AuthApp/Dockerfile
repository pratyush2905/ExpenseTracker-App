FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the prebuilt jar from the jars folder
COPY jars/app.jar app.jar

EXPOSE 9899

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
