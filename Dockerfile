# Build with a versioned tag: docker build -t serenity-backend:1.0.0 .
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
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /workspace/target/serenity-backend-0.0.1-SNAPSHOT.jar /app/app.jar
USER app
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD-SHELL wget -qO- "http://localhost:${PORT:-8081}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
