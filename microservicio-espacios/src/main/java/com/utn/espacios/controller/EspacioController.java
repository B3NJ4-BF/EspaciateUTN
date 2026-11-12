package com.utn.espacios.controller;

import com.utn.espacios.dto.CambiarEstadoRequestDTO;
import com.utn.espacios.dto.EspacioRequestDTO;
import com.utn.espacios.dto.EspacioResponseDTO;
import com.utn.espacios.entity.EstadoEspacio;
import com.utn.espacios.entity.TipoEspacio;
import com.utn.espacios.service.EspacioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/espacios")
@Validated
public class EspacioController {

    private final EspacioService espacioService;

    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @PostMapping
    public ResponseEntity<EspacioResponseDTO> crearEspacio(
            @Valid @RequestBody EspacioRequestDTO request) {
        EspacioResponseDTO creado = espacioService.crearEspacio(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacioResponseDTO> actualizarEspacio(
            @PathVariable @Positive(message = "El ID debe ser positivo") Long id,
            @Valid @RequestBody EspacioRequestDTO request) {
        return ResponseEntity.ok(espacioService.actualizarEspacio(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponseDTO> obtenerPorId(@PathVariable @Positive(message = "El ID debe ser positivo") Long id) {
        return ResponseEntity.ok(espacioService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EspacioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(espacioService.listarTodos());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EspacioResponseDTO>> listarPorEstado(
            @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(espacioService.listarPorEstado(estado));
    }

    @GetMapping("/tipo/{tipoEspacio}")
    public ResponseEntity<List<EspacioResponseDTO>> listarPorTipo(
            @PathVariable TipoEspacio tipoEspacio) {
        return ResponseEntity.ok(espacioService.listarPorTipo(tipoEspacio));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<EspacioResponseDTO>> buscarPorRequerimientos(
            @RequestParam @Min(value = 1, message = "La cantidad de personas debe ser mayor a cero.")
            int cantidadPersonas,
            @RequestParam(required = false) Boolean requiereProyector,
            @RequestParam(required = false) Boolean requiereLab) {
        return ResponseEntity.ok(espacioService.buscarDisponiblesPorFiltro(
                cantidadPersonas, requiereProyector, requiereLab));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EspacioResponseDTO> cambiarEstado(
            @PathVariable @Positive(message = "El ID debe ser positivo") Long id,
            @Valid @RequestBody CambiarEstadoRequestDTO request) {
        return ResponseEntity.ok(espacioService.cambiarEstado(id, request.estado()));
    }

       
                                                                                  
       
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLogicamente(@PathVariable @Positive(message = "El ID debe ser positivo") Long id) {
        espacioService.eliminarEspacio(id);
        return ResponseEntity.noContent().build();
    }
}
