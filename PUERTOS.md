# Puertos locales

| Componente | Puerto | URL |
|---|---:|---|
| API Gateway | 8080 | http://localhost:8080 |
| Reservas | 8081 | http://localhost:8081 |
| Usuarios | 8082 | http://localhost:8082 |
| Espacios | 8083 | http://localhost:8083 |
| Keycloak / emisor JWT | 8180 | http://localhost:8180 |

Las llamadas del cliente deben realizarse preferentemente a través del Gateway (`8080`). Los puertos `8081`, `8082` y `8083` son destinos internos/locales de los microservicios. El emisor JWT debe estar disponible en el puerto `8180` según la configuración actual del Gateway.

Para cambiar el puerto predeterminado de cada microservicio se puede definir `SERVER_PORT`. Reservas ya lo utiliza; Usuarios y Espacios también quedaron configurados para respetarlo.
