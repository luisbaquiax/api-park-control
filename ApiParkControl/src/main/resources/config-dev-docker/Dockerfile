FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY ApiParkControl.jar /app/ApiParkControl.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "ApiParkControl.jar"]