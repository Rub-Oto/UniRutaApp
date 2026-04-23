# 1. Usamos una imagen que tiene Java y Gradle listos
FROM gradle:7.6-jdk17 AS build

# 2. Creamos la carpeta de trabajo
WORKDIR /app

# 3. Copiamos todo tu código al servidor de Render
COPY --chown=gradle:gradle . .

# 4. Construimos el archivo ejecutable (JAR) ahí mismo
RUN gradle shadowJar --no-daemon

# 5. Pasamos a una imagen más ligera para correr la app
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app

# 6. Traemos el archivo que acabamos de construir
COPY --from=build /app/build/libs/*-all.jar app.jar

# 7. Exponemos el puerto y arrancamos
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]