package MicReservas.dto;

import MicReservas.Reserva;
import MicReservas.model.EstadoReserva;
import java.time.LocalDateTime;

   
                                                               
                                                                                      
   
public record ReservaResponse(
        Long id,
        Integer usuarioId,
        String nombreUsuario,
        String rolUsuario,
        Integer espacioId,
        String tipoEspacio,
        Integer cantidadEstudiantes,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        EstadoReserva estado
) {
                                                                                  
    public static ReservaResponse desde(Reserva r) {
        return new ReservaResponse(r.getId(), r.getUsuarioId(), r.getNombreUsuario(),
                r.getRolUsuario(), r.getEspacioId(), r.getTipoEspacio(),
                r.getCantidadEstudiantes(), r.getFechaHoraInicio(),
                r.getFechaHoraFin(), r.getEstado());
    }
}
