FROM openjdk:17-jdk-slim as builder

WORKDIR /app

COPY gradle gradle/
COPY gradlew ./
# Copy any .gradle files (supports kotline build.gradle.kts)
COPY *.gradle* ./

# Check gradlew has been given execute permissions and able to run
RUN chmod +x ./gradlew
RUN ./gradlew --help > /dev/null

COPY src src/

RUN ./gradlew clean build

# Note: don't run as root
FROM gcr.io/distroless/java17-debian11:debug

WORKDIR /app

COPY --from=builder /app/build/libs/application.jar /app/application.jar

ENTRYPOINT ["java", "-jar", "/app/application.jar"]