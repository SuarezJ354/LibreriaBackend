FROM maven:3.9.4-openjdk-17 AS build

WORKDIR /app

# Copiar pom.xml primero para aprovechar cache de Docker
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación
RUN mvn clean package -DskipTests

# Etapa final - runtime
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar JAR compilado desde etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]