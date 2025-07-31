./mvnw clean package
#./mvnw clean package -Dquarkus.package.jar.type=uber-jar
podman build -t quay.io/mcaballol/camel-rest-status:v3 .
podman push quay.io/mcaballol/camel-rest-status:v3