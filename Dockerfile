# -------------------------------------------------------------
# Estágio 1: Build da aplicação com Maven e JDK 17
# -------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copia apenas o pom.xml para aproveitar o cache de camadas do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia os fontes e compila o JAR executável
COPY src ./src
RUN mvn clean package -DskipTests -B

# -------------------------------------------------------------
# Estágio 2: Imagem final leve para execução (JRE 17 Alpine)
# -------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário não-root para boas práticas de segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia o artefato .jar gerado no estágio de build
COPY --from=builder /app/target/*.jar app.jar

# Variáveis de ambiente configuráveis (compatíveis com Coolify)
ENV PORT=38458 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expõe a porta padrão
EXPOSE 38458

# Executa o Spring Boot respeitando a porta definida pelo Coolify
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]

