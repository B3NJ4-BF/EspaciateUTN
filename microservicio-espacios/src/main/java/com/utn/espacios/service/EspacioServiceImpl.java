package com.utn.espacios.service;

import com.utn.espacios.dto.EspacioRequestDTO;
import com.utn.espacios.dto.EspacioResponseDTO;
import com.utn.espacios.entity.Espacio;
import com.utn.espacios.entity.EstadoEspacio;
import com.utn.espacios.entity.TipoEspacio;
import com.utn.espacios.exception.BadRequestException;
import com.utn.espacios.exception.ResourceNotFoundException;
import com.utn.espacios.repository.EspacioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepository;
    private final RestClient reservasClient;
    private final String internalToken;

    public EspacioServiceImpl(EspacioRepository espacioRepository,
                              @Value("${reservas.service.url:http://localhost:8081}") String reservasUrl,
                              @Value("${internal.api.token:dev-internal-token-change-me}") String internalToken) {
        this.espacioRepository = espacioRepository;
        this.reservasClient = RestClient.builder().baseUrl(reservasUrl).build();
        this.internalToken = internalToken;
    }

    @Override
    public EspacioResponseDTO crearEspacio(EspacioRequestDTO request) {
        Espacio espacio = new Espacio();
        aplicarDatos(espacio, request);
        return convertir(espacioRepository.save(espacio));
    }

    @Override
    public EspacioResponseDTO actualizarEspacio(Long id, EspacioRequestDTO request) {
        Espacio espacio = buscarEntidadPorId(id);
        aplicarDatos(espacio, request);
        return convertir(espacioRepository.save(espacio));
    }

    @Override
    @Transactional(readOnly = true)
    public EspacioResponseDTO obtenerPorId(Long id) {
        return convertir(buscarEntidadPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> listarTodos() {
        return espacioRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"))
                .stream().map(EspacioResponseDTO::desdeEntidad).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> listarPorEstado(EstadoEspacio estado) {
        if (estado == null) {
            throw new BadRequestException("El estado es obligatorio.");
        }
        return espacioRepository.findByEstado(estado)
                .stream().map(EspacioResponseDTO::desdeEntidad).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> listarPorTipo(TipoEspacio tipoEspacio) {
        if (tipoEspacio == null) {
            throw new BadRequestException("El tipo de espacio es obligatorio.");
        }
        return espacioRepository.findByTipoEspacio(tipoEspacio)
                .stream().map(EspacioResponseDTO::desdeEntidad).toList();
    }

    @Override
    public EspacioResponseDTO cambiarEstado(Long id, EstadoEspacio nuevoEstado) {
        if (nuevoEstado == null) {
            throw new BadRequestException("El nuevo estado es obligatorio.");
        }
        Espacio espacio = buscarEntidadPorId(id);
        espacio.setEstado(nuevoEstado);
        return convertir(espacioRepository.save(espacio));
    }

       
                                                                               
                                                                                        
       
    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> buscarDisponiblesPorFiltro(
            int cantidadPersonas,
            Boolean requiereProyector,
            Boolean requiereLab) {

        if (cantidadPersonas <= 0) {
            throw new BadRequestException("La cantidad de personas debe ser mayor a cero.");
        }

        return espacioRepository
                .findByEstadoAndCapacidadMaxGreaterThanEqual(EstadoEspacio.ACTIVO, cantidadPersonas)
                .stream()
                .filter(espacio -> espacio.cumpleConRequerimientos(
                        cantidadPersonas, requiereProyector, requiereLab))
                .map(EspacioResponseDTO::desdeEntidad)
                .toList();
    }

    @Override
    public void eliminarEspacio(Long id) {
        Espacio espacio = buscarEntidadPorId(id);
        try {
            reservasClient.delete().uri("/api/reservas/interno/espacios/{id}", id)
                    .header("X-Internal-Token", internalToken).retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudieron cancelar las reservas del espacio. No se eliminó el espacio.", ex);
        }
        espacioRepository.delete(espacio);
    }

    private Espacio buscarEntidadPorId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id debe ser un número positivo.");
        }
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un espacio con id " + id + "."));
    }

    private void aplicarDatos(Espacio espacio, EspacioRequestDTO request) {
        espacio.setNombre(request.nombre().trim());
        espacio.setTipoEspacio(request.tipoEspacio());
        espacio.setCapacidadMax(request.capacidadMax());
        espacio.setTieneProyector(request.tieneProyector());
        espacio.setEsLaboratorio(request.esLaboratorio());
        espacio.setUbicacion(request.ubicacion() == null ? null : request.ubicacion().trim());
        espacio.setEstado(request.estado());
    }

    private EspacioResponseDTO convertir(Espacio espacio) {
        return EspacioResponseDTO.desdeEntidad(espacio);
    }
}

