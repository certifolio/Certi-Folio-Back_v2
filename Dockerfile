FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon
RUN JAR_FILE="$(find build/libs -name '*SNAPSHOT.jar' ! -name '*plain.jar' | head -n 1)" \
    && cp "$JAR_FILE" /tmp/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /tmp/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
