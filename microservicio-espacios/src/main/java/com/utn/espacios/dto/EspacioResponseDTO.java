package com.utn.espacios.dto;

import com.utn.espacios.entity.Espacio;
import com.utn.espacios.entity.EstadoEspacio;
import com.utn.espacios.entity.TipoEspacio;

public record EspacioResponseDTO(
        Long id,
        String nombre,
        TipoEspacio tipoEspacio,
        Integer capacidadMax,
        Boolean tieneProyector,
        Boolean esLaboratorio,
        String ubicacion,
        EstadoEspacio estado) {

    public static EspacioResponseDTO desdeEntidad(Espacio espacio) {
        return new EspacioResponseDTO(
                espacio.getId(),
                espacio.getNombre(),
                espacio.getTipoEspacio(),
                espacio.getCapacidadMax(),
                espacio.getTieneProyector(),
                espacio.getEsLaboratorio(),
                espacio.getUbicacion(),
                espacio.getEstado());
    }
}
