# 1. Usamos una imagen de Java (JDK 17 es la estándar para Ktor)
FROM eclipse-temurin:17-jdk-focal

# 2. Creamos una carpeta para la app
WORKDIR /app

# 3. Copiamos el archivo "fat jar" que genera IntelliJ/Gradle
# Asegúrate de que el nombre coincida con el que genera tu proyecto
COPY build/libs/*-all.jar app.jar

# 4. Exponemos el puerto que usará Render
EXPOSE 8080

# 5. Comando para arrancar el servidor
CMD ["java", "-jar", "app.jar"]