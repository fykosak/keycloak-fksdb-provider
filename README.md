# FKSDB keycloak user provider

- Docs: https://www.keycloak.org/docs/latest/server_development/index.html#_user-storage-spi
- Referenční implementace: https://github.com/keycloak/keycloak-quickstarts/tree/latest/extension/user-storage-jpa

## Development

### Init

Třeba nainstalovat `maven` a nainstalovat dependencies spuštěním

```
mvn install
```

V `docker` složce třeba vytvořit podsložku `config`. Pokud se tak neudělá,
vytvoří se sama s root právy, tedy je potřeba upravit její práva přes `chown`
pro přístup. Po vytvoření je potřeba vykopírovat soubor
`conf/quarkus.properties.sample` do `docker/config/quarkus.properties`, kde je
třeba upravit připojení k DB. Obdobně tak je potřeba zkopírovat `domains.properties(.sample)`
a patřičně upravit seznam domén.
Pokud se chcete připojit k databázi na hostovacím
zařízení, je možné využít url `host.docker.internal`.

### Build a spuštění

Build JAR archivu

```
mvn clean package
```

Spuštění keycloaku (ve složce `docker`)

```
docker compose up
```

## Poznámky

### Keycloak nemá aktuální data z FKSDB

Keycloak admin -> User Federation -> FKSDB -> Cache policy and set cache settings

### Keycloak neposílá role

Keycloak admin -> Client Scopes -> roles -> Mappers -> realm roles -> Add to ID token -> true
