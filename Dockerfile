# Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa final con Ubuntu (más fácil para PostgreSQL)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Instalar Python y PostgreSQL client
RUN apt-get update && \
    apt-get install -y python3 python3-pip postgresql-client && \
    rm -rf /var/lib/apt/lists/*

# Crear enlace simbólico
RUN ln -s /usr/bin/python3 /usr/bin/python

# Instalar psycopg2 (más fácil en Ubuntu)
RUN pip3 install psycopg2-binary

# Limita el uso de RAM con Java flags livianos
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=70"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]