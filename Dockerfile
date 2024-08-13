FROM amazoncorretto:11-alpine
COPY target/dota2-winrate-service-4.0.0.jar app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/app.jar"]
