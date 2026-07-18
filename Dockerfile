FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/ai-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
