# Etapa 1: Build com Maven e Java 22 (cache amigável)
FROM maven:3.9.9-eclipse-temurin-22-alpine AS builder

WORKDIR /build

# Cache de dependências primeiro
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Agora traz o código-fonte
COPY src ./src

# Empacota (sem testes; ajuste se quiser rodá-los)
RUN mvn -B clean package -DskipTests

# Etapa 2: Runtime leve
FROM eclipse-temurin:22-jdk-alpine
# Usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

# Copia JAR da etapa de build
COPY --from=builder /build/target/*.jar /app/app.jar

# Permissões
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# JAVA_OPTS opcional (ex.: -Xms256m -Xmx512m)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
