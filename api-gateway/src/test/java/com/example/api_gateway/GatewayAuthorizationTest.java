package com.example.api_gateway;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/test")
@AutoConfigureWebTestClient
class GatewayAuthorizationTest {
    @Autowired WebTestClient client;

    @Test void rechazaSolicitudSinToken() {
        client.post().uri("/api/usuarios")
            .exchange().expectStatus().isUnauthorized();
    }

    @Test void estudianteNoPuedeCrearUsuarios() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .post().uri("/api/usuarios")
            .exchange().expectStatus().isForbidden();
    }

    @Test void estudianteNoPuedeCrearEspacios() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .post().uri("/api/v1/espacios")
            .exchange().expectStatus().isForbidden();
    }

    @Test void estudiantePuedeAccederAUnaRutaDeConsultaDeEspacios() {
                                                                                  
                                                                                      
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .get().uri("/api/v1/espacios")
            .exchange().expectStatus().value(code -> {
                org.junit.jupiter.api.Assertions.assertNotEquals(401, code);
                org.junit.jupiter.api.Assertions.assertNotEquals(403, code);
            });
    }
    @Test void tokenSinRolNoPuedeCrearReserva() {
        client.mutateWith(mockJwt())
            .post().uri("/api/reservas")
            .exchange().expectStatus().isForbidden();
    }

    @Test void docentePuedeConsultarEspacios() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))))
            .get().uri("/api/v1/espacios")
            .exchange().expectStatus().value(code -> {
                org.junit.jupiter.api.Assertions.assertNotEquals(401, code);
                org.junit.jupiter.api.Assertions.assertNotEquals(403, code);
            });
    }

    @Test void docenteNoPuedeCrearEspacios() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))))
            .post().uri("/api/v1/espacios")
            .exchange().expectStatus().isForbidden();
    }

    @Test void estudianteNoPuedeModificarEspacios() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .put().uri("/api/v1/espacios/1")
            .exchange().expectStatus().isForbidden();
    }

    @Test void adminPuedeIntentarCrearUsuarios() {
                                                                                          
                                                                    
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .post().uri("/api/usuarios")
            .exchange().expectStatus().value(code -> {
                org.junit.jupiter.api.Assertions.assertNotEquals(401, code);
                org.junit.jupiter.api.Assertions.assertNotEquals(403, code);
            });
    }

    @Test void estudianteNoPuedeConsultarListadoGlobalDeReservas() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .get().uri("/api/reservas")
            .exchange().expectStatus().isForbidden();
    }

    @Test void docenteNoPuedeConsultarReservaPorId() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))))
            .get().uri("/api/reservas/1")
            .exchange().expectStatus().isForbidden();
    }

    @Test void docentePuedeCrearReserva() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))))
            .post().uri("/api/reservas")
            .exchange().expectStatus().value(code -> {
                org.junit.jupiter.api.Assertions.assertNotEquals(401, code);
                org.junit.jupiter.api.Assertions.assertNotEquals(403, code);
            });
    }

    @Test void administradorPuedeConsultarListadoDeReservas() {
        client.mutateWith(mockJwt().authorities(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .get().uri("/api/reservas")
            .exchange().expectStatus().value(code -> {
                org.junit.jupiter.api.Assertions.assertNotEquals(401, code);
                org.junit.jupiter.api.Assertions.assertNotEquals(403, code);
            });
    }

}
