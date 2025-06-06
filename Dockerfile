FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

RUN apk update && apk add --no-cache \
    python3 \
    py3-pip \
    postgresql-libs \
    postgresql-dev \
    gcc \
    musl-dev \
    python3-dev \
    libffi-dev \
    openssl-dev

RUN pip3 install --upgrade pip
RUN pip3 install --no-cache-dir psycopg2-binary

RUN apk del gcc musl-dev python3-dev libffi-dev openssl-dev postgresql-dev

RUN rm -rf /var/cache/apk/*

COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/scripts ./scripts

ENV JAVA_TOOL_OPTIONS="-Xmx384m -Xms128m -XX:+UseG1GC"

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
