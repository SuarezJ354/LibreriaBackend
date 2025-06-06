# Etapa de compilación con Maven (Alpine)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa final con Java + Python + PostgreSQL (Alpine)
FROM eclipse-temurin:17-alpine

WORKDIR /app

# Instalar Python, pip, y dependencias para psycopg2
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

# Copiar JAR de compilación
COPY --from=build /app/target/*.jar app.jar

# Copiar scripts Python
COPY src/main/resources/scripts ./scripts

# Healthcheck opcional
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Configuración JVM optimizada
ENV JAVA_TOOL_OPTIONS="-Xmx384m -Xms128m -XX:+UseG1GC"

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
