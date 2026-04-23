FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .

# Esta línea es la clave: le da permisos al ejecutable dentro de Render
RUN chmod +x gradlew
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]