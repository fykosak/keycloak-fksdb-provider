# Dockerfile that build JAR of the provider, installs keycloak from github releases
# and puts it all together.
# Reason for manual install instead of keycloak image: keycloak image is based on RHEL9
# that needs x86-64-v2, which the current server does not support.

# build JAR using maven
FROM maven:3-eclipse-temurin-21 AS maven
WORKDIR /build/

COPY pom.xml .
RUN mvn install

COPY src src
RUN mvn clean package

# install everyting
FROM eclipse-temurin:21 AS builder
ARG version="26.2.5"
ARG archive_checksum="b0f6aa48625a090713ca0964cad47846af84ebc3"

WORKDIR /build/

# install install
ADD https://github.com/keycloak/keycloak/releases/download/${version}/keycloak-${version}.tar.gz keycloak.tar.gz
RUN echo "${archive_checksum} keycloak.tar.gz" | sha1sum -c -

RUN tar -xvzf keycloak.tar.gz
RUN mv keycloak-${version} keycloak

# install provider and run
FROM eclipse-temurin:21
WORKDIR /opt/keycloak

COPY fksdb-theme /opt/keycloak/themes/fksdb-theme

# enable health statistics
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

COPY --chmod=555 docker/entrypoint.sh /app/entrypoint.sh
COPY config/ /app/config/

COPY --from=builder --chown=1000:1000 /build/keycloak/ .
COPY --from=maven --chmod=644 /build/target/fksdb-keycloak-user-provider.jar providers/fksdb-provider.jar

# enable custom user to write to certain system files
RUN chmod -R 777 lib && mkdir -m 777 data

ENTRYPOINT /app/entrypoint.sh
