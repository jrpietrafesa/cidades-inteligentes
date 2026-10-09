# ============================================================
# Cidades ESG Inteligentes - imagem multi-stage
# Estagio 1: build com Maven | Estagio 2: runtime somente JRE
# ============================================================

# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia apenas o pom primeiro para aproveitar o cache de dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes ja rodam no pipeline (job build-test); aqui so empacotamos
RUN mvn -B -q package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

ARG APP_VERSION=dev
LABEL org.opencontainers.image.title="cidades-esg-inteligentes" \
      org.opencontainers.image.description="API de monitoramento energetico ESG" \
      org.opencontainers.image.version="${APP_VERSION}"

# Usuario sem privilegios (boa pratica de seguranca)
RUN addgroup -S esg && adduser -S esg -G esg
WORKDIR /app

COPY --from=build --chown=esg:esg /workspace/target/cidades-esg.jar app.jar

ENV APP_VERSION=${APP_VERSION} \
    TZ=America/Sao_Paulo \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

USER esg
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
