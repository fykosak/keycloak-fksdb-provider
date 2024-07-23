#!/bin/bash

cp --update=none /app/config/quarkus.properties.sample /opt/keycloak/conf/quarkus.properties

/opt/keycloak/bin/kc.sh start
