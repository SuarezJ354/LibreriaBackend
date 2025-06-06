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

# Instala Python para permitir exportaciones CSV
RUN apk update && apk add --no-cache python3 py3-pip

# Opcional: agrega librerías si las usa tu script (psycopg2, pandas, etc.)
RUN pip3 install --no-cache-dir psycopg2-binary

# Copia el jar y scripts Python
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/scripts ./scripts
RUN chmod +x ./scripts/*.py

# Limita el uso de RAM con flags eficientes
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=70"

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
