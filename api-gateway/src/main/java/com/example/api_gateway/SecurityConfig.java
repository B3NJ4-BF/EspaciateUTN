package com.example.api_gateway;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.http.HttpMethod;

@Configuration 
public class SecurityConfig {

    @Bean 
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http                                                                                                  
                .csrf(csrf -> csrf.disable())                                                  
                .authorizeExchange(auth -> auth

                    //Permisos para los roles :P
                    .pathMatchers(HttpMethod.POST, "/api/usuarios", "/api/usuarios/")
                        .hasRole("ADMIN") 
                    
                    .pathMatchers(HttpMethod.GET, "/api/usuarios/*")
                        .hasRole("ADMIN")

                    .pathMatchers(HttpMethod.GET, "/api/usuarios")
                        .hasRole("ADMIN" )

                    .pathMatchers(HttpMethod.POST, "/api/reservas")
                        .hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")
                                                                                                                                                   
                    .pathMatchers(HttpMethod.GET, "/api/reservas", "/api/reservas/*")
                        .hasRole("ADMIN")

                    .pathMatchers(HttpMethod.PUT, "/api/reservas/*")
                        .hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")

                    .pathMatchers(HttpMethod.PATCH, "/api/reservas/*/cancelacion")
                        .hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")

                    .pathMatchers(HttpMethod.GET,
                        "/api/v1/espacios",
                        "/api/v1/espacios/*",
                        "/api/v1/espacios/estado/*",
                        "/api/v1/espacios/tipo/*",
                        "/api/v1/espacios/buscar")
                        .hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")
                    
                    .pathMatchers(HttpMethod.POST, "/api/v1/espacios")
                        .hasRole("ADMIN")

                    .pathMatchers(HttpMethod.PUT, "/api/v1/espacios/*")
                        .hasRole("ADMIN")

                    .pathMatchers(HttpMethod.PATCH, "/api/v1/espacios/*/estado")
                        .hasRole("ADMIN")

                    .pathMatchers(HttpMethod.DELETE, "/api/v1/espacios/*")
                        .hasRole("ADMIN")
                    
                    .anyExchange().authenticated()
                )
                                                   
                // Configuracion de OAuth2 Resource Server con JWT 
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(
                            //adaptador para webflux
                            new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()) 
                        )
                    )
                )
                .build();
    }

    //convierte los roles de Keycloak a roles de Spring Security
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Map<String, List<String>> realmAccess = jwt.getClaim("realm_access");
            List<String> roles = realmAccess == null ? List.of() : realmAccess.getOrDefault("roles", List.of());
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    //normaliza los roles
                    .map(rol -> rol.toUpperCase(java.util.Locale.ROOT))
                    .map(rol -> "ADMINISTRADOR".equals(rol) ? "ADMIN" : rol)
                    .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                    .toList();

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

}