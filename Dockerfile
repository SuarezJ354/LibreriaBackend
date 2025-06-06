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

# Instalar Python en Alpine
RUN apk add --no-cache python3 py3-pip

# Crear enlace simbólico si es necesario
RUN ln -sf python3 /usr/bin/python

# Si tu script Python tiene dependencias, instálalas aquí
# RUN pip3 install pandas mysql-connector-python

# Limita el uso de RAM con Java flags livianos
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=70"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]