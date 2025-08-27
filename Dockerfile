
# Stage 1: Build the application
FROM maven:3.8.6-eclipse-temurin-17-alpine AS builder
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

COPY target/currency-converter-0.0.1-SNAPSHOT.jar app.jar

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port 8080
EXPOSE 8080





# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]