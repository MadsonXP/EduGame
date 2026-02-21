# Estágio 1: Construir o projeto
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Estágio 2: Rodar o projeto
FROM openjdk:17-jdk-slim
COPY --from=build /target/edugame-0.0.1-SNAPSHOT.jar edugame.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "edugame.jar"]