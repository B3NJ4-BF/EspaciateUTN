package com.utn.espacios.dto;

import com.utn.espacios.entity.EstadoEspacio;
import com.utn.espacios.entity.TipoEspacio;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EspacioRequestDTO(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres.")
        String nombre,

        @NotNull(message = "El tipo de espacio es obligatorio.")
        TipoEspacio tipoEspacio,

        @NotNull(message = "La capacidad máxima es obligatoria.")
        @Min(value = 1, message = "La capacidad máxima debe ser mayor a cero.")
        @Max(value = 500, message = "La capacidad máxima no puede superar 500.")
        Integer capacidadMax,

        Boolean tieneProyector,
        Boolean esLaboratorio,

        @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres.")
        String ubicacion,

        EstadoEspacio estado) {

    public EspacioRequestDTO {
        if (tieneProyector == null) {
            tieneProyector = false;
        }
        if (esLaboratorio == null) {
            esLaboratorio = false;
        }
        if (estado == null) {
            estado = EstadoEspacio.ACTIVO;
        }
    }
}
