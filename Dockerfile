# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Package Stage
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/demo-aseguradoras-1.0.0-SNAPSHOT.jar demo-aseguradoras.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "demo-aseguradoras.jar"]