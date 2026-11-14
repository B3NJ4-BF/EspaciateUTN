# Sistema de gestión de reservas de espacios

Proyecto académico compuesto por cuatro aplicaciones Spring Boot: un API Gateway y tres microservicios independientes para reservas, usuarios y espacios.

## Requisitos

- JDK 17.
- Maven 3.6.3 o superior.
- Keycloak disponible en `http://localhost:8180`, con el realm `tpi-reservas` configurado para emitir los JWT esperados por el sistema.

## Estructura

- `api-gateway`: punto de entrada HTTP y validación de tokens.
- `microservicio-reservas`: gestión de reservas y, opcionalmente, integración con Google Calendar.
- `usuario-service`: gestión de usuarios.
- `microservicio-espacios`: catálogo y gestión de espacios físicos.
- `postman`: recursos para probar la API.

## Ejecución local

Abrí una terminal por aplicación y ejecutá los comandos desde la carpeta indicada. Iniciá primero los microservicios y luego el Gateway.

**1. Usuarios** (puerto 8082)

```bash
cd usuario-service
mvn spring-boot:run
```

**2. Espacios** (puerto 8083)

```bash
cd microservicio-espacios
mvn spring-boot:run
```

**3. Reservas** (puerto 8081)

```bash
cd microservicio-reservas
mvn spring-boot:run
```

**4. API Gateway** (puerto 8080)

```bash
cd api-gateway
mvn spring-boot:run
```

Las solicitudes de los clientes deben dirigirse preferentemente al Gateway: `http://localhost:8080`. Los microservicios quedan disponibles localmente en los puertos indicados.

## Demostración con Docker Compose

Con Java 17, Maven y Docker disponibles, ejecutá desde la raíz del repositorio:

```bash
# Crear configuración local de secretos
cp .env.example .env
# Editar .env y reemplazar INTERNAL_API_TOKEN por un secreto largo

mvn -f api-gateway/pom.xml clean package
mvn -f usuario-service/pom.xml clean package
mvn -f microservicio-espacios/pom.xml clean package
mvn -f microservicio-reservas/pom.xml clean package
docker compose up --build -d
```

En una instalación nueva, Keycloak importa el realm `tpi-reservas` y Docker inicializa los volúmenes de Usuarios y Espacios con las bases H2 de sus carpetas `data/`. Las bases ya contienen seis usuarios y seis espacios. Cada cuenta tiene un `usuario_id` que coincide con el ID en Usuarios.

| Cuentas | Rol | IDs |
|---|---|---|
| `admin1` | Administrador | 1 |
| `alumno1`, `alumno2`, `alumno3` | Estudiante | 2, 3, 4 |
| `docente1`, `docente2` | Docente | 5, 6 |

Las seis cuentas usan la contraseña de demo `EspaciateDemo2026`. En Postman, enviá un POST a `http://localhost:8180/realms/tpi-reservas/protocol/openid-connect/token` con un cuerpo `x-www-form-urlencoded` que incluya `grant_type=password`, `client_id=postman-tpi`, `username` y `password`. Con el token de `admin1`, hacé un GET a `http://localhost:8080/api/usuarios` usando Bearer Token para ver los seis registros.

Si querés ensayar una instalación nueva conservando tus datos locales, usá otro nombre de proyecto con `docker compose -p espaciate-demo up --build -d` y asegurate de que los puertos estén libres. Los volúmenes con datos existentes conservan las bases y el realm de Keycloak.

## Puertos

| Componente | Puerto | Dirección |
|---|---:|---|
| API Gateway | 8080 | `http://localhost:8080` |
| Reservas | 8081 | `http://localhost:8081` |
| Usuarios | 8082 | `http://localhost:8082` |
| Espacios | 8083 | `http://localhost:8083` |
| Keycloak | 8180 | `http://localhost:8180` |

## Pruebas

Para ejecutar las pruebas de cada aplicación, desde su carpeta:

```bash
mvn test
```

## Configuración

Los servicios permiten ajustar puertos y conexiones mediante variables de entorno. Entre las principales se encuentran `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DDL_AUTO` y `KEYCLOAK_ISSUER_URI`. El valor predeterminado del emisor JWT es `http://localhost:8180/realms/tpi-reservas`.

La integración con Google Calendar está deshabilitada por defecto. Para habilitarla con Docker Compose, colocar la credencial en `secrets/google-calendar.json`, establecer `GOOGLE_CALENDAR_ENABLED=true` y configurar `GOOGLE_CALENDAR_ID` en `.env`. En ejecución local se configura `GOOGLE_CALENDAR_CREDENTIALS_FILE` con la ruta del JSON. Las llamadas remotas reintentan hasta tres veces; si persiste el error, se conserva la operación local y se registra el fallo para reconciliación posterior.

Las bases H2 se configuran por servicio y se crean al iniciar cada aplicación según su configuración. No se requiere compartir una base de datos entre microservicios.

## Seguridad de eliminación coordinada

La eliminación de usuarios y espacios está reservada a ADMIN en el Gateway. Antes de borrar el registro, cada servicio solicita a Reservas cancelar las reservas activas relacionadas mediante endpoints internos protegidos por `X-Internal-Token`. Definir el mismo `INTERNAL_API_TOKEN` en `.env` para los tres servicios; no usar el valor de ejemplo en entornos compartidos.
