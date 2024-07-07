FROM quay.io/keycloak/keycloak:latest as builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:latest
COPY --from=builder /opt/keycloak/ /opt/keycloak/

ADD --chown=keycloak:keycloak --chmod=644 target/fksdb-keycloak-user-provider-0.0.1.jar /opt/keycloak/providers/fksdb-provider.jar

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
