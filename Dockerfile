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

# Instalar Python y dependencias para PostgreSQL en Alpine
RUN apk add --no-cache python3 py3-pip py3-setuptools postgresql-dev gcc musl-dev python3-dev

# Crear enlace simbólico si es necesario
RUN ln -sf python3 /usr/bin/python

# Instalar psycopg2 para PostgreSQL (necesita compilación en Alpine)
RUN pip3 install --no-cache-dir psycopg2==2.9.5

# Crear directorio para scripts si no existe
RUN mkdir -p /app/scripts

# Limita el uso de RAM con Java flags livianos
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=70"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]