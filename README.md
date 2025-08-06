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
# Editar el script build-and-push.sh
cat > build-and-push.sh << 'EOF'
#!/bin/bash

# Variables de configuración
REGISTRY="quay.io"
USERNAME="[tu-usuario]"
IMAGE_NAME="camel-rest-status"
VERSION="${1:-v1.0}"
DATE_TAG=$(date +%Y%m%d)

echo "🏗️  Compilando aplicación..."
./mvnw clean package

if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación"
    exit 1
fi

echo "🐳 Construyendo imagen Docker..."
podman build -t ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${VERSION} .

if [ $? -ne 0 ]; then
    echo "❌ Error construyendo imagen"
    exit 1
fi

echo "🏷️  Creando tags adicionales..."
podman tag ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${VERSION} ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:latest
podman tag ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${VERSION} ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${DATE_TAG}

echo "📤 Publicando imágenes en Quay.io..."
podman push ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${VERSION}
podman push ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:latest
podman push ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${DATE_TAG}

if [ $? -eq 0 ]; then
    echo "✅ Imágenes publicadas exitosamente:"
    echo "   - ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${VERSION}"
    echo "   - ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:latest"
    echo "   - ${REGISTRY}/${USERNAME}/${IMAGE_NAME}:${DATE_TAG}"
else
    echo "❌ Error publicando imágenes"
    exit 1
fi
EOF

# Hacer ejecutable el script
chmod +x build-and-push.sh

# Ejecutar con versión específica
./build-and-push.sh v1.1

# O ejecutar con versión por defecto
./build-and-push.sh
```

## Despliegue en OpenShift/Kubernetes

### Manifiestos Kubernetes

#### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: camel-rest-status
  labels:
    app: camel-rest-status
spec:
  replicas: 2
  selector:
    matchLabels:
      app: camel-rest-status
  template:
    metadata:
      labels:
        app: camel-rest-status
    spec:
      containers:
      - name: camel-rest-status
        image: quay.io/[tu-usuario]/camel-rest-status:v1.0
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: CUSTOM_MESSAGE
          value: "Hello from Camel REST Status Service"
        livenessProbe:
          httpGet:
            path: /info
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /info
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

#### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  # not to use camel-rest-status because the service have conflict because the service generates the environment CAMEL_REST_STATUS_SERVICE_PORT
  name: camel-status
  labels:
    app: camel-rest-status
spec:
  selector:
    app: camel-rest-status
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

#### Route (OpenShift)

```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: camel-rest-status
  labels:
    app: camel-rest-status
spec:
  to:
    kind: Service
    name: camel-status
  port:
    targetPort: http
  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Redirect
```

### Comandos de Despliegue

```bash
# Desplegar en OpenShift/Kubernetes
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/route.yaml  # Solo en OpenShift

# Verificar despliegue
kubectl get pods -l app=camel-rest-status
kubectl get svc camel-rest-status
kubectl get route camel-rest-status  # Solo en OpenShift

# Ver logs
kubectl logs -f deployment/camel-rest-status

# Probar el servicio
kubectl port-forward svc/camel-rest-status 8080:8080
curl http://localhost:8080/info
```

## Configuración y Variables de Entorno

| Variable | Descripción | Valor por Defecto | Requerida |
|----------|-------------|-------------------|-----------|
| `POD_NAME` | Nombre del pod | - | No |
| `POD_NAMESPACE` | Namespace del pod | - | No |
| `POD_IP` | IP del pod | - | No |
| `CUSTOM_MESSAGE` | Mensaje personalizado | - | No |
| `QUARKUS_HTTP_PORT` | Puerto HTTP | 8080 | No |

## Monitoreo y Logs

### Health Checks

El servicio expone el endpoint `/info` que puede ser utilizado para health checks:

```bash
# Health check básico
curl -f http://localhost:8080/info

# Con timeout
timeout 5 curl -f http://localhost:8080/info
```

### Logs de Aplicación

```bash
# Ver logs en tiempo real
kubectl logs -f deployment/camel-rest-status

# Ver logs de un pod específico
kubectl logs camel-rest-status-[pod-id]

# Ver logs con timestamp
kubectl logs --timestamps=true deployment/camel-rest-status
```

## Troubleshooting

### Problemas Comunes

#### 1. Error de Compilación

```bash
# Limpiar y recompilar
./mvnw clean
./mvnw compile

# Verificar versión de Java
java -version
```

#### 2. Error de Conexión a Quay.io

```bash
# Verificar conectividad
ping quay.io

# Re-autenticarse
podman logout quay.io
podman login quay.io

# Verificar credenciales
podman login --get-login quay.io
```

#### 3. Pod no inicia en Kubernetes

```bash
# Verificar eventos
kubectl describe pod [pod-name]

# Verificar imagen
kubectl get pods -o jsonpath='{.items[*].spec.containers[*].image}'

# Verificar recursos
kubectl top pods
```

#### 4. Endpoint no responde

```bash
# Verificar puerto del contenedor
kubectl get svc camel-rest-status -o yaml

# Probar conectividad interna
kubectl exec -it [pod-name] -- curl localhost:8080/info

# Verificar variables de entorno
kubectl exec [pod-name] -- env | grep POD
```

## Desarrollo

### Modo Desarrollo

```bash
# Ejecutar en modo desarrollo (hot reload)
./mvnw quarkus:dev

# La aplicación estará disponible en http://localhost:8080
# Los cambios se recargan automáticamente
```

### Testing

```bash
# Ejecutar tests
./mvnw test

# Test con coverage
./mvnw test jacoco:report

# Test de integración
./mvnw verify
```

### Perfiles de Build

```bash
# Perfil de desarrollo
./mvnw quarkus:dev

# Perfil de producción
./mvnw clean package -Pproduction

# Perfil nativo
./mvnw clean package -Pnative
```

## Contribución

1. Fork del repositorio
2. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -am 'Agregar nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Crear Pull Request

## Licencia

Este proyecto está bajo la Licencia Apache 2.0 - ver el archivo [LICENSE](LICENSE) para detalles.

## Soporte

Para reportar bugs o solicitar features, por favor crear un issue en el repositorio del proyecto.
