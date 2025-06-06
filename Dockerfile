# Imagen base solo para ejecutar el JAR y los scripts
FROM python:3.11-slim AS runtime

WORKDIR /app

# Instala solo lo mínimo para psycopg2-binary
RUN apt-get update && \
    apt-get install -y gcc libpq-dev && \
    pip install --no-cache-dir psycopg2-binary

# Copia el JAR ya compilado desde tu máquina
COPY target/*.jar app.jar

# Copia los scripts
COPY src/main/resources/scripts ./scripts

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
