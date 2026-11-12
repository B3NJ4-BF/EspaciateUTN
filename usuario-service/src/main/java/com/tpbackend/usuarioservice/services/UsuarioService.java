package com.tpbackend.usuarioservice.services;

import com.tpbackend.usuarioservice.dtos.UsuarioRequestDTO;
import com.tpbackend.usuarioservice.dtos.UsuarioResponseDTO;
import com.tpbackend.usuarioservice.models.Usuario;
import com.tpbackend.usuarioservice.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RestClient reservasClient;
    private final String internalToken;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          @Value("${reservas.service.url:http://localhost:8081}") String reservasUrl,
                          @Value("${internal.api.token:dev-internal-token-change-me}") String internalToken) {
        this.usuarioRepository = usuarioRepository;
        this.reservasClient = RestClient.builder().baseUrl(reservasUrl).build();
        this.internalToken = internalToken;
    }
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

                                                                  
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya se encuentra registrado.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(normalizarRol(dto.getRol()))
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return mapearAResponseDTO(usuarioGuardado);
    }

    private String normalizarRol(String rol) {
        String r = rol == null ? "" : rol.trim().toUpperCase(java.util.Locale.ROOT);
        if (r.equals("ADMIN")) r = "ADMINISTRADOR";
        if (!java.util.Set.of("ESTUDIANTE", "DOCENTE", "ADMINISTRADOR").contains(r))
            throw new IllegalArgumentException("Rol inválido. Use ESTUDIANTE, DOCENTE o ADMINISTRADOR.");
        return r;
    }

                                      
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
        return mapearAResponseDTO(usuario);
    }

                                   
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

                                                                           
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con ID: " + id));
        try {
            reservasClient.delete().uri("/api/reservas/interno/usuarios/{id}", id)
                    .header("X-Internal-Token", internalToken).retrieve().toBodilessEntity();
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudieron cancelar las reservas del usuario. No se eliminó el usuario.", ex);
        }
        usuarioRepository.delete(usuario);
    }

    private UsuarioResponseDTO mapearAResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
