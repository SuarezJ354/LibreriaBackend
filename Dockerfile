FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .

ENV MAVEN_OPTS="-Xmx1024m"
RUN mvn dependency:resolve

COPY src/ ./src/

RUN mvn clean package -DskipTests -q

RUN ls -la target/

EXPOSE 8080

CMD ["sh", "-c", "java -Xmx512m -jar target/*.jar"]
