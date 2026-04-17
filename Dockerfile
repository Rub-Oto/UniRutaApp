# Usamos una imagen de Java para correr el servidor
FROM openjdk:11-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiamos los archivos del proyecto
COPY . .

# Permisos para ejecutar gradle
RUN chmod +x gradlew

# Compilamos el proyecto
RUN ./gradlew build

# Exponemos el puerto (Render lo manejará internamente)
EXPOSE 8080

# Comando para iniciar Ktor
CMD ["./gradlew", "run"]
