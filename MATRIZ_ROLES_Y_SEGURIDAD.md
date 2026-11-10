# Matriz unificada de roles y seguridad

| Recurso | Método | ADMIN | DOCENTE | ESTUDIANTE |
|---|---|---:|---:|---:|
| `/api/usuarios` | POST, GET | Sí | No | No |
| `/api/usuarios/{id}` | GET | Sí | No | No |
| `/api/reservas` | POST | Sí | Sí | Sí |
| `/api/reservas` y `/{id}` | GET | Sí | No | No |
| `/api/reservas/{id}` | PUT | Sí | Titular | Titular |
| `/api/reservas/{id}/cancelacion` | PATCH | Sí | Titular | Titular |
| `/api/v1/espacios/**` | GET | Sí | Sí | Sí |
| `/api/v1/espacios` | POST | Sí | No | No |
| `/api/v1/espacios/{id}` | PUT, DELETE | Sí | No | No |
| `/api/v1/espacios/{id}/estado` | PATCH | Sí | No | No |

Roles Keycloak: `ADMIN` o `ADMINISTRADOR` (normalizados a `ROLE_ADMIN`), `DOCENTE`, `ESTUDIANTE`.

## Claim de titularidad

PUT/PATCH de Reservas requieren que el JWT validado incluya `usuario_id` (o `usuarioId`) con el ID numérico del registro de Usuarios. Se obtiene del JWT, nunca del body. Reservas valida el token mediante `KEYCLOAK_ISSUER_URI` además del Gateway. Si falta el claim, los usuarios comunes no pueden modificar/cancelar; ADMIN puede operar sobre cualquier reserva.

Ejemplo: `{"iss":"http://localhost:8180/realms/tpi-reservas","sub":"keycloak-sub","usuario_id":12,"realm_access":{"roles":["ESTUDIANTE"]}}`.

## ADMIN inicial y sincronización

1. Crear realm `tpi-reservas`.
2. Crear manualmente el primer usuario administrador en la consola Keycloak.
3. Asignarle rol de realm `ADMIN` (o `ADMINISTRADOR`).
4. Crear el registro equivalente en Usuarios con rol `ADMINISTRADOR`.
5. Configurar un protocol mapper para emitir `usuario_id` con el ID de Usuarios.
6. Verificar `iss`, `realm_access.roles` y `usuario_id` en el token.

El aprovisionamiento/sincronización es un procedimiento operativo documentado, no una automatización implementada en este TP. No guardar secretos reales en el repositorio.

## Configuración

`KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/tpi-reservas`
