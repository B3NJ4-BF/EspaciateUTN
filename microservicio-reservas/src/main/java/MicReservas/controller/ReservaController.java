package MicReservas.controller;

import MicReservas.Reserva;
import MicReservas.dto.CrearReservaRequest;
import MicReservas.dto.ModificarReservaRequest;
import MicReservas.dto.ReservaResponse;
import MicReservas.model.EstadoReserva;
import MicReservas.service.ReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

   
                                                                             
                                                                             
   
@RestController
@Validated
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService service;

                                                                                   
    public ReservaController(ReservaService service) { this.service = service; }

                                                                        
                                                                                              
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@Valid @RequestBody CrearReservaRequest request,
                                 @AuthenticationPrincipal Jwt jwt,
                                 Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return ReservaResponse.desde(service.crear(request, usuarioId(jwt), admin));
    }

                                                                       
    @GetMapping("/{id}")
    public ReservaResponse buscar(@PathVariable @Positive(message = "El ID debe ser positivo") Long id) {
        return ReservaResponse.desde(service.buscarPorId(id));
    }

                                                                                  
    @GetMapping
    public List<ReservaResponse> listar(
            @RequestParam(required = false) @Positive(message = "El usuarioId debe ser positivo") Integer usuarioId,
            @RequestParam(required = false) @Positive(message = "El espacioId debe ser positivo") Integer espacioId,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.listar(usuarioId, espacioId, estado, desde, hasta)
                .stream().map(ReservaResponse::desde).toList();
    }

                                                               
    @PutMapping("/{id}")
    public ReservaResponse modificar(@PathVariable @Positive(message = "El ID debe ser positivo") Long id,
                                     @Valid @RequestBody ModificarReservaRequest request,
                                     @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return ReservaResponse.desde(service.modificar(id, request, usuarioId(jwt), admin));
    }

                                                                                 
    @PatchMapping("/{id}/cancelacion")
    public ReservaResponse cancelar(@PathVariable @Positive(message = "El ID debe ser positivo") Long id,
                                    @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return ReservaResponse.desde(service.cancelar(id, usuarioId(jwt), admin));
    }

    private Integer usuarioId(Jwt jwt) {
        Object claim = jwt.getClaims().getOrDefault("usuario_id", jwt.getClaims().get("usuarioId"));
        if (claim instanceof Number n) return n.intValue();
        if (claim instanceof String value) {
            try { return Integer.valueOf(value); } catch (NumberFormatException ignored) { }
        }
        return null;
    }


}
