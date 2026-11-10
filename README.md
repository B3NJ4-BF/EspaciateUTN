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

La integración con Google Calendar está deshabilitada por defecto. Para habilitarla se deben configurar `GOOGLE_CALENDAR_ENABLED`, `GOOGLE_CALENDAR_CREDENTIALS_FILE` y `GOOGLE_CALENDAR_ID`.

Las bases H2 se configuran por servicio y se crean al iniciar cada aplicación según su configuración. No se requiere compartir una base de datos entre microservicios.
