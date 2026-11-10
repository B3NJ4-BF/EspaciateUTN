FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/microservicio-espacios-1.0.0.jar app.jar
RUN mkdir -p /app/data
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
