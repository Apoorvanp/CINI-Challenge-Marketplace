# Installation Steps
### Pre-requisites
* Docker
* Java 17
### Spin up
```shell
./gradlew clean bootBuildImage
docker compose up
```
### Tear down
```shell
docker compose down --volumes
```