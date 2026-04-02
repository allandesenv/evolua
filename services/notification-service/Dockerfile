FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . /workspace
RUN mvn -pl services/notification-service -am -B clean package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/notification-service/target/notification-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
