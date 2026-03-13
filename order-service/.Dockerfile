# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar
# Expose port (will be overridden by docker-compose)
EXPOSE 8080 
ENTRYPOINT ["java", "-jar", "app.jar"]