FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/survey_bot-1.0.0.jar /app/application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]

EXPOSE 8080