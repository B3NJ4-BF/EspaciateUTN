package MicReservas.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CrearReservaRequest(
        @Positive(message = "El usuarioId debe ser positivo") Integer usuarioId,
        @NotNull(message = "El espacioId es obligatorio") @Positive(message = "El espacioId debe ser positivo") Integer espacioId,
        @NotNull(message = "La cantidadEstudiantes es obligatoria")
        @Positive(message = "La cantidad debe ser positiva") @Max(value = 500, message = "La cantidad no puede superar 500") Integer cantidadEstudiantes,
        @NotNull(message = "La fechaHoraInicio es obligatoria") LocalDateTime fechaHoraInicio
) {}
