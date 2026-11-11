FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/usuario-service-0.0.1-SNAPSHOT.jar app.jar
COPY data/usuarios-db.mv.db /app/data/usuarios-db.mv.db
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
