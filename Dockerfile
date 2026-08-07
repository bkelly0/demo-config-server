FROM gradle:9.5.1-jdk17 AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle ./
COPY src src

RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]