FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/sentinel-server-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /var/log/app
ENTRYPOINT ["java", "-jar", "app.jar"]