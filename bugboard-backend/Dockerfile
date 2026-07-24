# Phase 1: Build using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy configuration files to download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and compile
COPY src ./src
RUN mvn clean package

# Phase 2: Runtime (use a lightweight image with only JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only the JAR file generated from the build phase
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]