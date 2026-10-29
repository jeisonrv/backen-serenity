# Build reproducible con la misma versión de Java que usa el proyecto.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.title="Serenity Backend" \
      org.opencontainers.image.source="https://github.com/jeisonrv/backen-serenity"
WORKDIR /app
RUN apk add --no-cache curl
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /workspace/target/serenity-backend-0.0.1-SNAPSHOT.jar /app/app.jar
USER app
EXPOSE 8081
ENV JAVA_OPTS="-Xms1g -Xmx2g"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
