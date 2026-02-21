# Estagio 1: Construir o projeto
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Estagio 2: Rodar o projeto (Imagem atualizada e ativa)
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /target/edugame-0.0.1-SNAPSHOT.jar edugame.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "edugame.jar"]