# build JAR using maven
FROM maven:3-eclipse-temurin-21 AS maven
WORKDIR /build/

COPY pom.xml .
RUN mvn install

COPY src src
RUN mvn clean package

# mount provider, build and run
FROM quay.io/keycloak/keycloak:25.0.1 AS builder
WORKDIR /opt/keycloak/

# enable health statistics
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

COPY --chown=keycloak:keycloak --chmod=744 docker/entrypoint.sh /app/entrypoint.sh
COPY --chown=keycloak:keycloak config/ /app/config/

COPY --from=maven --chown=keycloak:keycloak --chmod=644 /build/target/fksdb-keycloak-user-provider.jar providers/fksdb-provider.jar

ENTRYPOINT /app/entrypoint.sh
