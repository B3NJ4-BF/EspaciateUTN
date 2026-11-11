FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/microservicio-espacios-1.0.0.jar app.jar
COPY data/espacios-db.mv.db /app/data/espacios-db.mv.db
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
