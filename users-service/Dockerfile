# Multi-stage Dockerfile for movimentacao-service
# Builder image - uses Gradle with JDK 21 to build the Spring Boot fat jar
FROM gradle:8.6-jdk21 AS builder
WORKDIR /home/gradle/project
# copy project and set ownership for Gradle user
COPY --chown=gradle:gradle . .
# build the executable jar (skip tests for faster local builds)
RUN gradle --no-daemon clean bootJar -x test

# Runtime image - lightweight JRE
FROM eclipse-temurin:21-jre
ARG JAVA_OPTS=""
EXPOSE 8080
# copy jar from builder stage
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
