# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
RUN ls -la /app/services/target/

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]