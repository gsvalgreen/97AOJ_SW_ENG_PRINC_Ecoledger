# Multi-stage Dockerfile for certificacao-service
FROM gradle:8.6-jdk21 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
ARG JAVA_OPTS=""
EXPOSE 8085
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
