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
- La integración usa llamadas síncronas a Google Calendar. Si Calendar está habilitado y la API falla, la operación de reserva falla para evitar una reserva sin evento sincronizado. Para producción sería recomendable usar patrón Outbox/reintentos.
- Para configurar OAuth de usuario final (cada usuario autoriza su propio calendario) se requiere un flujo OAuth con pantalla de consentimiento y almacenamiento/renovación de tokens; no es el enfoque elegido para este TP.
