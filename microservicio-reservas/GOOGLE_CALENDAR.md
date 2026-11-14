# Integración Google Calendar en Reservas

## Funcionamiento

- Al crear una reserva, Reservas persiste el registro y crea un evento en Google Calendar.
- El identificador del evento queda guardado en `google_calendar_event_id`.
- Al modificar una reserva, actualiza el evento remoto.
- Al cancelar, elimina el evento remoto y conserva la reserva como `CANCELADA` en la base local.
- Se usa zona horaria `America/Argentina/Buenos_Aires` y la duración que determina Reservas (1 hora).
- La integración está apagada por defecto para que el proyecto pueda ejecutarse localmente sin credenciales.

## Configuración (Google Cloud)

1. En Google Cloud Console, crear/seleccionar un proyecto.
2. Habilitar **Google Calendar API**.
3. Crear una cuenta de servicio y generar una clave JSON. Guardarla fuera del repositorio (no subirla a Git).
4. Copiar el correo de la cuenta de servicio (termina en `iam.gserviceaccount.com`).
5. En Google Calendar, compartir el calendario objetivo con ese correo y otorgar permiso **Hacer cambios en eventos**.
6. Copiar el ID del calendario (en Configuración del calendario → Integrar calendario). No necesariamente es `primary`.

## Variables de entorno

PowerShell, antes de ejecutar Reservas:

```powershell
$env:GOOGLE_CALENDAR_ENABLED="true"
$env:GOOGLE_CALENDAR_CREDENTIALS_FILE="C:\secrets\google-calendar-service-account.json"
$env:GOOGLE_CALENDAR_ID="calendar-id@group.calendar.google.com"
$env:GOOGLE_CALENDAR_TIME_ZONE="America/Argentina/Buenos_Aires"
```

Luego iniciar el microservicio con `mvn spring-boot:run`.

En desarrollo local, dejar `GOOGLE_CALENDAR_ENABLED=false` (valor predeterminado).

## Consideraciones

- No guardar el JSON de credenciales en el ZIP, en el repositorio ni en `src/main/resources`.
- La cuenta de servicio no representa al usuario final; crea eventos en el calendario compartido.
- Las operaciones remotas se reintentan hasta tres veces ante errores. Si Google continúa sin responder, la operación local de reserva/cambio/cancelación se conserva y el error queda registrado en los logs; por eso puede existir una diferencia temporal entre la base local y el calendario.
- Para operaciones de creación que no consiguieron crear el evento, no se dispone todavía de una cola Outbox ni de una tarea automática de reconciliación. Para producción se recomienda incorporar Outbox con estado de sincronización y reintentos persistentes.
- Si falla la eliminación de un evento ya asociado, se conserva su identificador en la reserva para facilitar una reconciliación posterior.
- Para configurar OAuth de usuario final (cada usuario autoriza su propio calendario) se requiere un flujo OAuth con pantalla de consentimiento y almacenamiento/renovación de tokens; no es el enfoque elegido para este TP.

## Docker Compose

Desde la raíz del proyecto:

1. Crear `.env` copiando `.env.example` y reemplazar `INTERNAL_API_TOKEN` por un secreto aleatorio largo.
2. Para activar Calendar, colocar el JSON de la cuenta de servicio en `secrets/google-calendar.json` (la carpeta está ignorada por Git).
3. En `.env`, establecer `GOOGLE_CALENDAR_ENABLED=true` y completar `GOOGLE_CALENDAR_ID`.
4. Ejecutar `docker compose up --build -d`.

Compose monta `secrets/` como solo lectura en Reservas y configura `GOOGLE_CALENDAR_CREDENTIALS_FILE=/run/secrets/google-calendar.json`. Si Calendar está deshabilitado, no hace falta crear el archivo de credenciales.
