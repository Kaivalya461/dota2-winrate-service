FROM amazoncorretto:21-alpine
COPY target/dota2-winrate-service-5.0.0.jar app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/app.jar"]
