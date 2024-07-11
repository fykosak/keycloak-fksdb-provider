# FKSDB keycloak user provider
  - Docs: https://www.keycloak.org/docs/latest/server_development/index.html#_user-storage-spi
  - Reference implementation: https://github.com/keycloak/keycloak-quickstarts/tree/latest/extension/user-storage-jpa


## Init
```
mvn install
```

## Build and run
```
mvn clean package
docker compose up --remove-orphans --build
```
