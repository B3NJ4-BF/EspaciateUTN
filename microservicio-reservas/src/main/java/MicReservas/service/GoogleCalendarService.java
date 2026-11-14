package MicReservas.service;

import MicReservas.Reserva;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;

                                                                              
@Service
public class GoogleCalendarService {
    private final String credentialsPath;
    private final String calendarId;
    private final String zoneId;
    private final boolean enabled;

    public GoogleCalendarService(
            @Value("${google.calendar.enabled:false}") boolean enabled,
            @Value("${google.calendar.credentials-file:}") String credentialsPath,
            @Value("${google.calendar.calendar-id:primary}") String calendarId,
            @Value("${google.calendar.time-zone:America/Argentina/Buenos_Aires}") String zoneId) {
        this.enabled = enabled;
        this.credentialsPath = credentialsPath;
        this.calendarId = calendarId;
        this.zoneId = zoneId;
    }

    public String crearEvento(Reserva reserva) {
        if (!enabled) return null;
        try {
            Event created = ejecutarConReintentos(() ->
                    getCalendar().events().insert(calendarId, toEvent(reserva)).execute());
            return created.getId();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear el evento en Google Calendar: " + e.getMessage(), e);
        }
    }

    public void actualizarEvento(Reserva reserva) {
        if (!enabled || reserva.getGoogleCalendarEventId() == null) return;
        try {
            ejecutarConReintentos(() -> getCalendar().events()
                    .update(calendarId, reserva.getGoogleCalendarEventId(), toEvent(reserva)).execute());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo actualizar el evento de Google Calendar: " + e.getMessage(), e);
        }
    }

    public void cancelarEvento(Reserva reserva) {
        if (!enabled || reserva.getGoogleCalendarEventId() == null) return;
        try {
            ejecutarConReintentos(() -> getCalendar().events()
                    .delete(calendarId, reserva.getGoogleCalendarEventId()).execute());
            reserva.setGoogleCalendarEventId(null);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cancelar el evento de Google Calendar: " + e.getMessage(), e);
        }
    }

    /**
     * Reintenta fallos transitorios de red/servicio antes de informar el error.
     * El estado de la reserva en la base local no depende de la disponibilidad de Google.
     */
    private <T> T ejecutarConReintentos(Callable<T> operacion) throws Exception {
        Exception ultimoError = null;
        for (int intento = 1; intento <= 3; intento++) {
            try {
                return operacion.call();
            } catch (Exception ex) {
                ultimoError = ex;
                if (intento < 3) {
                    try {
                        Thread.sleep(300L * intento);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw interrupted;
                    }
                }
            }
        }
        throw ultimoError;
    }

    private Calendar getCalendar() throws Exception {
        if (credentialsPath == null || credentialsPath.isBlank())
            throw new IllegalStateException("Falta configurar GOOGLE_CALENDAR_CREDENTIALS_FILE.");
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of(CalendarScopes.CALENDAR));
        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("Espaciate - Reservas").build();
    }

    private Event toEvent(Reserva r) {
        String title = "Reserva " + r.getTipoEspacio() + " - Espacio " + r.getEspacioId();
        String description = "Reserva ID: " + r.getId() + "\nUsuario: " + r.getNombreUsuario()
                + " (ID " + r.getUsuarioId() + ")\nCantidad de estudiantes: " + r.getCantidadEstudiantes();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZoneId zone = ZoneId.of(zoneId);
        EventDateTime start = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(
                r.getFechaHoraInicio().atZone(zone).format(formatter))).setTimeZone(zoneId);
        EventDateTime end = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(
                r.getFechaHoraFin().atZone(zone).format(formatter))).setTimeZone(zoneId);
        return new Event().setId(r.getGoogleCalendarEventId()).setSummary(title).setDescription(description).setStart(start).setEnd(end);
    }
}
