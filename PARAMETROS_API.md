# Parámetros de creación

## Usuarios — `POST /api/usuarios`

```json
{
  "nombre": "Ana",
  "apellido": "Pérez",
  "email": "ana@ejemplo.com",
  "password": "ClaveSegura123",
  "rol": "ESTUDIANTE"
}
```

Campos obligatorios: `nombre`, `apellido`, `email`, `password`, `rol`.
Roles permitidos: `ESTUDIANTE`, `DOCENTE`, `ADMINISTRADOR` (también se acepta `ADMIN` y se normaliza).

## Espacios — `POST /api/v1/espacios`

```json
{
  "nombre": "Aula 101",
  "tipoEspacio": "AULA_COMUN",
  "capacidadMax": 30,
  "tieneProyector": false,
  "esLaboratorio": false,
  "ubicacion": "Edificio principal, primer piso",
  "estado": "ACTIVO"
}
```

Obligatorios: `nombre`, `tipoEspacio`, `capacidadMax`.
Opcionales: `tieneProyector` (default `false`), `esLaboratorio` (default `false`), `ubicacion`, `estado` (default `ACTIVO`).
Tipos válidos: `AULA_COMUN`, `LABORATORIO`, `BOX_ESTUDIO`. Capacidad máxima: 500.

## Reservas — `POST /api/reservas`

El request se simplificó para no duplicar datos que ya están en Usuarios y Espacios.

```json
{
  "espacioId": 10,
  "cantidadEstudiantes": 5,
  "fechaHoraInicio": "2026-11-16T10:00:00"
}
```

Para un usuario común, el titular se obtiene del claim `usuario_id` (o `usuarioId`) del JWT. Un administrador puede agregar `usuarioId` para indicar al titular:

```json
{
  "usuarioId": 1,
  "espacioId": 10,
  "cantidadEstudiantes": 5,
  "fechaHoraInicio": "2026-11-16T10:00:00"
}
```

Ya no se envían `nombreUsuario`, `rolUsuario` ni `tipoEspacio`: se consultan en los microservicios correspondientes usando los IDs. La duración es de una hora y el horario permitido es de 08:00 a 22:00.

## Recomendaciones

- Mantener `usuarioId` opcional en el request: para estudiantes/docentes se debe tomar siempre del JWT, evitando que reserven a nombre de otra persona.
- Mantener `tipoEspacio` y datos del titular fuera del request de reserva para evitar inconsistencias y manipulación del cliente.
- Validar que `cantidadEstudiantes` no supere la capacidad del espacio; además `BOX_ESTUDIO` admite hasta 4 y aulas/laboratorios requieren al menos 5.
- La eliminación de usuarios y espacios cancela primero las reservas activas relacionadas. Si el microservicio de reservas no responde, la eliminación se rechaza para no dejar reservas huérfanas.
- Configurar `INTERNAL_API_TOKEN` con un secreto largo y aleatorio en cualquier entorno compartido/producción. El valor por defecto solo sirve para desarrollo local.
