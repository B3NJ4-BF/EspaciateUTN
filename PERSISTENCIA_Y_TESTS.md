# Persistencia y plan de pruebas reforzado

## 1. Persistencia de Usuarios y Espacios

Usuarios y Espacios dejaron de usar `jdbc:h2:mem:...` como base predeterminada.
Ahora utilizan H2 en modo archivo:

- Usuarios: `./data/usuarios-db`
- Espacios: `./data/espacios-db`
- Reservas ya utilizaba H2 en archivo: `./data/reservas-api`

Los archivos se crean dentro de la carpeta `data` del microservicio desde cuyo directorio se inicia la aplicación. Al detener y volver a iniciar el servicio, los registros permanecen.

Variables configurables:

| Variable | Valor predeterminado | Uso |
|---|---|---|
| `DB_URL` | URL H2 de archivo del servicio | Permite cambiar la URL/motor de base |
| `DB_USER` | `sa` | Usuario de base |
| `DB_PASSWORD` | vacío | Contraseña local |
| `DDL_AUTO` | `update` | Actualización automática del esquema |
| `H2_CONSOLE_ENABLED` | `false` | Consola H2, solo habilitar temporalmente en desarrollo |

**Importante:** H2 en archivo resuelve la persistencia local para desarrollo y demostración; no es la recomendación para producción ni para varios servidores concurrentes. Para producción conviene PostgreSQL administrado, credenciales externas y migraciones versionadas (Flyway/Liquibase). No borrar la carpeta `data` si se quieren conservar los registros.

El `data.sql` de Espacios ahora es idempotente: los datos de ejemplo se insertan solo si no existe ya un espacio con ese nombre. Esto evita duplicados cada vez que se reinicia la aplicación.

### Comprobación manual de persistencia

1. Iniciar Usuarios desde su carpeta (`mvn spring-boot:run`) y crear un registro con `POST http://localhost:8082/api/usuarios`.
2. Anotar el ID devuelto, detener el proceso y volver a iniciar el servicio.
3. Consultar `GET http://localhost:8082/api/usuarios/{id}`: el registro debe seguir existiendo.
4. Repetir la prueba con Espacios (`POST/GET http://localhost:8083/api/v1/espacios`).

Ejecutar cada servicio desde su propia carpeta para que `./data` apunte siempre al mismo lugar. Si se inicia desde otro directorio, definir `DB_URL` con una ruta absoluta para evitar crear otra base en una ubicación distinta. No configurar `DDL_AUTO=create` ni `create-drop` en la ejecución normal: el valor predeterminado es `update`.

## 2. Validaciones agregadas

- Usuarios: nombre/apellido con longitud máxima, email válido y limitado, contraseña entre 8 y 100 caracteres y rol dentro del catálogo permitido.
- Espacios: capacidad entre 1 y 500; enums desconocidos, campos obligatorios y tipos JSON incorrectos se rechazan.
- Reservas: IDs positivos, cantidad entre 1 y 500, nombre/rol/tipo obligatorios, rol y tipo de espacio restringidos y fecha con formato válido.
- Gateway: pruebas de JWT sin token, rol insuficiente y conversión de roles (`ADMINISTRADOR` a `ROLE_ADMIN`).

Las entradas con letras en campos numéricos no se convierten silenciosamente: Jackson responde `400 Bad Request`.

## 3. Ejecutar pruebas

Ejecutar `mvn test` desde cada carpeta:

```bash
cd api-gateway && mvn test
cd usuario-service && mvn test
cd microservicio-espacios && mvn test
cd microservicio-reservas && mvn test
```

En Windows se puede usar `mvnw.cmd test` donde exista el wrapper, o `mvn test` si Maven está instalado.

Las pruebas de integración usan H2 en memoria con una base aislada para no tocar las bases persistentes locales. El servicio real usa H2 en archivo.

## 4. Matriz de pruebas negativas para Postman

Usar el token correspondiente al rol requerido en Gateway. Los códigos indicados se esperan en el microservicio; Gateway puede rechazar antes con `401` o `403`.

### API Gateway (8080)

| Caso | Petición | Resultado |
|---|---|---|
| Sin token | `POST /api/usuarios` | 401 |
| ESTUDIANTE intenta alta | `POST /api/usuarios` | 403 |
| ESTUDIANTE intenta crear espacio | `POST /api/v1/espacios` | 403 |
| DOCENTE consulta espacios | `GET /api/v1/espacios` | Pasa autorización |
| ESTUDIANTE consulta reservas globales | `GET /api/reservas` | 403 |
| DOCENTE consulta reserva por ID | `GET /api/reservas/1` | 403 |
| ADMIN consulta reservas | `GET /api/reservas` | Pasa autorización (requiere servicio activo) |
| JWT con rol `ADMINISTRADOR` | Operación ADMIN | Se interpreta como `ROLE_ADMIN` |
| ESTUDIANTE elimina usuario | `DELETE /api/usuarios/1` | 403 |
| ADMIN elimina usuario | `DELETE /api/usuarios/1` | Pasa autorización; requiere servicios activos |
| JWT sin `realm_access.roles` | Ruta protegida | Sin permisos de rol |

### Usuarios (directo 8082 o Gateway 8080)

| Caso | Datos / ruta | Resultado |
|---|---|---|
| Alta correcta | Nombre, apellido, email válido, contraseña >= 8, rol permitido | 201 |
| Nombre vacío/espacios | `nombre: "   "` | 400 |
| Apellido vacío | `apellido: ""` | 400 |
| Email inválido | `email: "abc"` | 400 |
| Email duplicado | Repetir email existente | 409 |
| Contraseña vacía | `password: ""` | 400 |
| Contraseña corta | `password: "123"` | 400 |
| Rol inventado | `rol: "SUPERADMIN"` | 400 |
| Rol ausente | Omitir `rol` | 400 |
| ID con letras | `GET /api/usuarios/abc` | 400 |
| Usuario inexistente | `GET /api/usuarios/999999` | 404 |
| Eliminar usuario con reservas activas | `DELETE /api/usuarios/{id}` | Reservas activas canceladas y usuario eliminado |
| Reservas no disponible al eliminar usuario | `DELETE /api/usuarios/{id}` | 503; usuario no eliminado |

### Espacios (directo 8083 o Gateway 8080)

| Caso | Datos / ruta | Resultado |
|---|---|---|
| Alta correcta | Capacidad positiva y tipo válido | 201 |
| Nombre vacío | `nombre: ""` | 400 |
| Tipo nulo | `tipoEspacio: null` | 400 |
| Tipo inexistente | `tipoEspacio: "AUDITORIO"` | 400 |
| Capacidad cero | `capacidadMax: 0` | 400 |
| Capacidad negativa | `capacidadMax: -5` | 400 |
| Capacidad con letras | `capacidadMax: "muchos"` | 400 |
| Capacidad excesiva | `capacidadMax: 501` | 400 |
| Cantidad de búsqueda negativa | `GET /buscar?cantidadPersonas=-2` | 400 |
| Cantidad de búsqueda con letras | `GET /buscar?cantidadPersonas=abc` | 400 |
| ID con letras | `GET /api/v1/espacios/abc` | 400 |
| Espacio inexistente | `GET /api/v1/espacios/999999` | 404 |
| Eliminar espacio con reservas activas | `DELETE /api/v1/espacios/{id}` | Reservas activas canceladas y espacio eliminado |
| Reservas no disponible al eliminar espacio | `DELETE /api/v1/espacios/{id}` | 503; espacio no eliminado |
| Estado inventado | `PATCH /{id}/estado` con `{"estado":"DISPONIBLE"}` | 400 |

### Reservas (directo 8081 o Gateway 8080)

| Caso | Datos / ruta | Resultado |
|---|---|---|
| Usuario ID negativo/cero | `usuarioId: -1` o `0` | 400 |
| Espacio ID negativo/cero | `espacioId: -1` o `0` | 400 |
| Cantidad negativa/cero | `cantidadEstudiantes: -3` o `0` | 400 |
| Cantidad con letras | `cantidadEstudiantes: "cinco"` | 400 |
| Cantidad excesiva | `cantidadEstudiantes: 501` | 400 |
| Nombre vacío | `nombreUsuario: " "` | 400 |
| Rol inventado | `rolUsuario: "INVITADO"` | 400 |
| Tipo vacío/inexistente | `tipoEspacio: ""` o `"CANCHA"` | 400 |
| Fecha inválida | `fechaHoraInicio: "mañana"` | 400 |
| ID de ruta con letras | `GET /api/reservas/abc` | 400 |
| Estado inexistente | `GET /api/reservas?estado=PENDIENTE` | 400 |
| Usuario remoto inexistente | ID que no exista en Usuarios | 400 (regla de integración) |
| Espacio remoto inexistente | ID que no exista en Espacios | 400 (regla de integración) |
| Espacio no activo | Espacio EN_MANTENIMIENTO/INHABILITADO | 400 |
| Capacidad excedida | Solicitar más personas que capacidadMax | 400 |
| Tipo distinto al catálogo | Enviar tipo que no coincida con Espacios | 400 |
| Reserva superpuesta | Mismo espacio y franja horaria | 409 |
| Cuarta reserva activa | Usuario ya tiene 3 reservas activas | 409 |
| Anticipación menor a 3 horas | Inicio demasiado próximo | 400 |
| Estudiante con más de 24 horas | Inicio fuera del límite | 400 |
| Docente con más de 48 horas | Inicio fuera del límite | 400 |
| Fuera de horario | Antes de 08:00 o después de 21:00 de inicio | 400 |
| Minutos no cero | Inicio 10:30 | 400 |
| Dos turnos consecutivos | Uno termina cuando el otro empieza | 201 para ambos |

## 5. Qué falta para una entrega de producción

- Reemplazar H2 por PostgreSQL y gestionar el esquema con Flyway/Liquibase.
- Agregar pruebas de contrato entre Reservas y los servicios remotos Usuarios/Espacios.
- Probar Gateway integrado con Keycloak real y los cuatro servicios levantados.
- Evaluar concurrencia en reservas simultáneas para impedir doble asignación bajo carga.
- Mantener contraseñas, claves y secretos fuera del repositorio.

## 6. Resultado de implementación y límites de verificación

Se incorporaron pruebas unitarias de cancelación masiva por usuario/espacio y pruebas de autorización DELETE de usuarios en Gateway. Google Calendar realiza hasta tres intentos por operación; si persiste un error, se conserva la operación local y se registra el problema. Todavía no existe una cola Outbox ni reconciliación automática para eventos que no llegaron a crearse.

La ejecución completa de Maven y el levantamiento de Docker Compose deben verificarse en un entorno con acceso al repositorio Maven y Docker. En el entorno de edición no se pudo descargar Maven desde Maven Central, por lo que los tests no se reportan como ejecutados exitosamente.
