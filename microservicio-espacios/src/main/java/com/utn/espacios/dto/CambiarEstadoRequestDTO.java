package com.utn.espacios.dto;

import com.utn.espacios.entity.EstadoEspacio;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequestDTO(
        @NotNull(message = "El estado es obligatorio.")
        EstadoEspacio estado) {
}
