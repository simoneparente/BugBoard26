# Fase 1: Build (usa un'immagine con Maven e JDK)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia i file di configurazione per scaricare le dipendenze
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia il codice sorgente e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Fase 2: Runtime (usa un'immagine leggera solo con JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia solo il file JAR generato dalla fase di build
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]