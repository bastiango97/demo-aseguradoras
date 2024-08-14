# Step 1: Build the project using Maven
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the application using the OpenJDK runtime
FROM openjdk:17-jdk-slim
COPY --from=build /target/your-jar-file-name.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
