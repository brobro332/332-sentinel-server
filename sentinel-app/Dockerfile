FROM gradle:8.5-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle clean build -x test --no-daemon --no-build-cache

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

COPY .env .env

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]