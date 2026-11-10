package com.tpbackend.usuarioservice.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
}
