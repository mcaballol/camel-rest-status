# Camel REST Status Service

## Descripción

El **Camel REST Status Service** es un microservicio desarrollado con **Quarkus** y **Apache Camel** que proporciona información de estado del pod en el que se ejecuta. Este servicio expone un endpoint REST que devuelve información sobre el pod, namespace, IP y mensajes personalizados.

## Características

- **Framework**: Quarkus 3.20.1 con Apache Camel
- **Java**: OpenJDK 17
- **Arquitectura**: Microservicio REST
- **Contenedorización**: Docker/Podman compatible
- **Despliegue**: OpenShift/Kubernetes ready

## Estructura del Proyecto

```
camel-rest-status/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/example/
│       │       └── RestRouteBuilder.java
│       ├── resources/
│       │   └── application.properties
│       └── docker/
│           ├── Dockerfile.jvm
│           ├── Dockerfile.native
│           └── Dockerfile.legacy-jar
├── target/                    # Directorio de build
├── pom.xml                   # Configuración Maven
├── Dockerfile               # Dockerfile principal
├── build-and-push.sh       # Script de build y push
└── README.md               # Esta documentación
```

## API Endpoints

### GET /info

Devuelve información del estado actual del pod.

**Respuesta:**
```json
{
  "podName": "camel-rest-status-xxx-xxx",
  "namespace": "default",
  "podIP": "10.244.0.15",
  "message": "Hello from Camel REST Status Service"
}
```

**Variables de entorno utilizadas:**
- `POD_NAME`: Nombre del pod
- `POD_NAMESPACE`: Namespace donde se ejecuta el pod
- `POD_IP`: IP del pod
- `CUSTOM_MESSAGE`: Mensaje personalizado

## Compilación

### Prerrequisitos

- **Java 17** o superior
- **Maven 3.6+** o usar el wrapper incluido
- **Podman** o **Docker** para containerización

### Compilación Local

#### 1. Compilación JVM (Recomendado)

```bash
# Usando Maven wrapper
./mvnw clean package

# O usando Maven instalado
mvn clean package
```

#### 2. Compilación Nativa (Opcional)

```bash
# Compilación nativa para mejor performance
./mvnw clean package -Dnative
```

#### 3. Compilación Uber JAR (Opcional)

```bash
# Genera un JAR con todas las dependencias
./mvnw clean package -Dquarkus.package.jar.type=uber-jar
```

### Verificar Compilación

```bash
# Verificar que el JAR fue generado
ls -la target/quarkus-app/

# Ejecutar localmente para pruebas
java -jar target/quarkus-app/quarkus-run.jar
```

## Containerización

### Build de Imagen Docker/Podman

#### Usando Podman (Recomendado)

```bash
# 1. Compilar la aplicación
./mvnw clean package

# 2. Construir la imagen
podman build -t quay.io/[tu-usuario]/camel-rest-status:v1.0 .

# 3. Verificar la imagen
podman images | grep camel-rest-status
```

#### Usando Docker

```bash
# 1. Compilar la aplicación
./mvnw clean package

# 2. Construir la imagen
docker build -t quay.io/[tu-usuario]/camel-rest-status:v1.0 .

# 3. Verificar la imagen
docker images | grep camel-rest-status
```

### Prueba Local del Contenedor

```bash
# Ejecutar el contenedor localmente
podman run -p 8080:8080 \
  -e POD_NAME="test-pod" \
  -e POD_NAMESPACE="test-namespace" \
  -e POD_IP="127.0.0.1" \
  -e CUSTOM_MESSAGE="Testing locally" \
  quay.io/[tu-usuario]/camel-rest-status:v1.0

# Probar el endpoint
curl http://localhost:8080/info
```

## Publicación en Quay.io

### 1. Configuración de Autenticación

#### Crear Token de Robot Account

1. Acceder a [Quay.io](https://quay.io)
2. Navegar a tu organización o usuario
3. Ir a **Account Settings** → **Robot Accounts**
4. Crear un nuevo Robot Account:
   - **Name**: `camel-rest-status-pusher`
   - **Description**: `Robot account for pushing camel-rest-status images`
   - **Permissions**: `Write` en el repositorio

#### Configurar Autenticación Local

```bash
# Opción 1: Login interactivo
podman login quay.io
# Introducir: Username: [tu-usuario]+[robot-account-name]
# Introducir: Password: [robot-account-token]

# Opción 2: Login con token directamente
echo '[robot-account-token]' | podman login quay.io --username '[tu-usuario]+[robot-account-name]' --password-stdin

# Verificar autenticación
podman login --get-login quay.io
```

### 2. Crear Repositorio en Quay.io

1. Acceder a [Quay.io](https://quay.io)
2. Click en **Create New Repository**
3. Configurar:
   - **Repository Name**: `camel-rest-status`
   - **Visibility**: `Public` o `Private` según necesidad
   - **Description**: `Camel REST Status microservice`

### 3. Push de la Imagen

#### Push Manual

```bash
# 1. Tag la imagen con diferentes versiones
podman tag quay.io/[tu-usuario]/camel-rest-status:v1.0 quay.io/[tu-usuario]/camel-rest-status:latest
podman tag quay.io/[tu-usuario]/camel-rest-status:v1.0 quay.io/[tu-usuario]/camel-rest-status:$(date +%Y%m%d)

# 2. Push de las imágenes
podman push quay.io/[tu-usuario]/camel-rest-status:v1.0
podman push quay.io/[tu-usuario]/camel-rest-status:latest
podman push quay.io/[tu-usuario]/camel-rest-status:$(date +%Y%m%d)

# 3. Verificar push exitoso
podman search quay.io/[tu-usuario]/camel-rest-status
```

#### Usando el Script de Automatización

```bash

# Hacer ejecutable el script
chmod +x build-and-push.sh

# Ejecutar con versión específica
./build-and-push.sh v1.1

# O ejecutar con versión por defecto
./build-and-push.sh
```
