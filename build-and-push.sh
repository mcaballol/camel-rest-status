mvn clean package
podman build --platform linux/amd64 -t quay.io/mcaballol/camel-rest-status .
podman push quay.io/mcaballol/camel-rest-status