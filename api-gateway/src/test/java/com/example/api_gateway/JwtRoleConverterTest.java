package com.example.api_gateway;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.junit.jupiter.api.Assertions.*;

class JwtRoleConverterTest {
    private final SecurityConfig config = new SecurityConfig();

    private Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2027-01-01T00:00:00Z"))
                .claims(c -> c.putAll(claims))
                .build();
    }

    @Test void convierteRolEstudianteDesdeRealmAccess() {
        var token = config.jwtAuthenticationConverter().convert(
                jwt(Map.of("realm_access", Map.of("roles", List.of("ESTUDIANTE")))));
        assertNotNull(token);
        assertTrue(token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ESTUDIANTE")));
    }

    @Test void normalizaAdministradorAAdmin() {
        var token = config.jwtAuthenticationConverter().convert(
                jwt(Map.of("realm_access", Map.of("roles", List.of("ADMINISTRADOR")))));
        assertNotNull(token);
        assertTrue(token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertFalse(token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
    }

    @Test void normalizaRolesSinDistinguirMayusculas() {
        var token = config.jwtAuthenticationConverter().convert(
                jwt(Map.of("realm_access", Map.of("roles", List.of("docente")))));
        assertNotNull(token);
        assertTrue(token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCENTE")));
    }

    @Test void jwtSinRealmAccessNoOtorgaRoles() {
        var token = config.jwtAuthenticationConverter().convert(jwt(Map.of("sub", "usuario-test")));
        assertNotNull(token);
        assertTrue(token.getAuthorities().isEmpty());
    }

    @Test void realmAccessSinListaDeRolesNoOtorgaRoles() {
        var token = config.jwtAuthenticationConverter().convert(
                jwt(Map.of("realm_access", Map.of())));
        assertNotNull(token);
        assertTrue(token.getAuthorities().isEmpty());
    }
}
