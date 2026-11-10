package MicReservas.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

                                                                      
public record ModificarReservaRequest(
        @NotBlank(message = "El tipoEspacio es obligatorio")
        @Pattern(regexp = "(?i)AULA_COMUN|LABORATORIO|BOX_ESTUDIO", message = "Tipo de espacio inválido") String tipoEspacio,
        @NotNull(message = "La cantidadEstudiantes es obligatoria")
        @Positive(message = "La cantidad debe ser positiva") @Max(value = 500, message = "La cantidad no puede superar 500") Integer cantidadEstudiantes,
        @NotNull(message = "La fechaHoraInicio es obligatoria") LocalDateTime fechaHoraInicio
) {}
