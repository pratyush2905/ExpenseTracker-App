FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9821
ENTRYPOINT ["java", "-jar", "app.jar"]