# FKSDB keycloak user provider
Docs: https://www.keycloak.org/docs/latest/server_development/index.html#_user-storage-spi
Possible reference implementation: https://github.com/nicolabeghin/keycloak-multiple-ds-user-storage 

## Init
```
mvn install
```

## Build and run
```
mvn clean package
docker compose up --remove-orphans --build
```
