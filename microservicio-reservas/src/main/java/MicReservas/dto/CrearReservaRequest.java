package MicReservas.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

                                                           
public record CrearReservaRequest(
        @Positive(message = "El usuarioId debe ser positivo") Integer usuarioId,
        @NotBlank(message = "El nombreUsuario es obligatorio") @Size(max = 120) String nombreUsuario,
        @NotBlank(message = "El rolUsuario es obligatorio")
        @Pattern(regexp = "(?i)ESTUDIANTE|DOCENTE|ADMIN|ADMINISTRADOR", message = "Rol inválido") String rolUsuario,
        @NotNull(message = "El espacioId es obligatorio") @Positive(message = "El espacioId debe ser positivo") Integer espacioId,
        @NotBlank(message = "El tipoEspacio es obligatorio")
        @Pattern(regexp = "(?i)AULA_COMUN|LABORATORIO|BOX_ESTUDIO", message = "Tipo de espacio inválido") String tipoEspacio,
        @NotNull(message = "La cantidadEstudiantes es obligatoria")
        @Positive(message = "La cantidad debe ser positiva") @Max(value = 500, message = "La cantidad no puede superar 500") Integer cantidadEstudiantes,
        @NotNull(message = "La fechaHoraInicio es obligatoria") LocalDateTime fechaHoraInicio
) {}
