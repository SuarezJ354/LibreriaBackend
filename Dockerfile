# Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa final: Java + Python + PostgreSQL en base Alpine real
FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

# Instalar Python, pip y psycopg2
RUN apk add --no-cache \
    python3 \
    py3-pip \
    postgresql-libs \
    postgresql-dev \
    gcc \
    musl-dev \
    python3-dev \
    libffi-dev \
    openssl-dev \
    && pip3 install --no-cache-dir psycopg2-binary \
    && apk del gcc musl-dev python3-dev libffi-dev openssl-dev postgresql-dev

# Copiar JAR y scripts
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/scripts ./scripts

# JVM config
ENV JAVA_TOOL_OPTIONS="-Xmx384m -Xms128m -XX:+UseG1GC"

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
