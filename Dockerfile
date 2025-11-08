# ====================================================================
# ETAPA 1: BUILDER (Compilación y Empaquetado)
# ====================================================================
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

# Ejecutamos el PAQUETE COMPLETO
# Si esto falla, veremos el error de compilación de Java
RUN mvn clean package -DskipTests

# ====================================================================
# ETAPA 2: RUNNER (Aplicación Final)
# ====================================================================
FROM eclipse-temurin:21-jre-jammy

EXPOSE 8080
ENV PORT=8080

# Usamos el comodín para copiar el JAR
COPY --from=builder /app/target/*.jar /app.jar

# Define el comando de inicio de la aplicación Java
ENTRYPOINT ["java", "-jar", "/app.jar"]