FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/ai-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
