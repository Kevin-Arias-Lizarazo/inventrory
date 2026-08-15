FROM gradle:jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/inventario-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV INVENTARIO_DB=/data/inventario.db \
    APP_UPLOADS_DIR=/data/uploads \
    TZ=America/Bogota

VOLUME ["/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]