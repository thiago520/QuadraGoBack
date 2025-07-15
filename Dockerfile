# Etapa 1: Build com Maven e Java 22
FROM maven:3.9.6-eclipse-temurin-22-alpine AS builder

# Diretório de trabalho no container
WORKDIR /build

# Copia arquivos do projeto para dentro do container
COPY pom.xml .
COPY src ./src

# Compila o projeto e gera o JAR (sem testes, se desejar remova o -DskipTests)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime com JDK 22 leve (sem Maven)
FROM eclipse-temurin:22-jdk-alpine

# Criar usuário não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Diretório onde a aplicação será executada
WORKDIR /app

# Copiar o JAR gerado da etapa anterior
COPY --from=builder /build/target/*.jar app.jar

# Dar permissão ao usuário appuser
RUN chown -R appuser:appgroup /app
USER appuser

# Expõe a porta padrão da aplicação
EXPOSE 8080

# Comando de execução com suporte a JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
