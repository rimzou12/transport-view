# syntax=docker/dockerfile:1

# --- Build stage ---
FROM maven:3.10.0-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/transport-view-backend-*.jar app.jar
USER appuser

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
