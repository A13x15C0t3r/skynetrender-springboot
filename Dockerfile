FROM amazoncorretto:25-alpine-jdk

COPY tarjet/skynet-core-service-0.0.1-SNAPSHOT.jar /api-v1.jar

ENTRYPOINT ["java", "-jar", "/api-v1.jar"]