# Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa final (ejecución liviana)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Limita el uso de RAM con Java flags livianos
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=70"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]