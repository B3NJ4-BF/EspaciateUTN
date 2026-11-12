package com.utn.espacios.service;

import com.utn.espacios.dto.EspacioRequestDTO;
import com.utn.espacios.dto.EspacioResponseDTO;
import com.utn.espacios.entity.EstadoEspacio;
import com.utn.espacios.entity.TipoEspacio;
import java.util.List;

public interface EspacioService {

    EspacioResponseDTO crearEspacio(EspacioRequestDTO request);

    EspacioResponseDTO actualizarEspacio(Long id, EspacioRequestDTO request);

    EspacioResponseDTO obtenerPorId(Long id);

    List<EspacioResponseDTO> listarTodos();

    List<EspacioResponseDTO> listarPorEstado(EstadoEspacio estado);

    List<EspacioResponseDTO> listarPorTipo(TipoEspacio tipoEspacio);

    EspacioResponseDTO cambiarEstado(Long id, EstadoEspacio nuevoEstado);
    
    void eliminarEspacio(Long id);

    List<EspacioResponseDTO> buscarDisponiblesPorFiltro(
            int cantidadPersonas,
            Boolean requiereProyector,
            Boolean requiereLab);
}
