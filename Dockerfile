# Dockerfile optimizado para Railway
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copiar archivos de configuración Maven primero
COPY pom.xml .

# Resolver dependencias (aprovecha cache de Docker)
RUN mvn dependency:resolve

# Copiar código fuente
COPY src ./src

# Compilar con más memoria y sin tests
RUN MAVEN_OPTS="-Xmx1024m" mvn clean package -DskipTests -q

# Verificar que el JAR existe
RUN ls -la target/

# Exponer puerto
EXPOSE 8080

# Ejecutar aplicación
CMD ["sh", "-c", "java -Xmx512m -jar target/*.jar"]