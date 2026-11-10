package com.example.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GWconfig {

    @Bean
    public RouteLocator configurarRutas(
            RouteLocatorBuilder builder,
            @Value("${app.usuarios.url}") String usuariosUrl,
            @Value("${app.espacios.url}") String espaciosUrl,
            @Value("${app.reservas.url}") String reservasUrl) {
        return builder.routes()
            .route("usuarios", r -> r
                .path("/api/usuarios/**")
                .uri(usuariosUrl))
            .route("espacios", r -> r
                .path("/api/v1/espacios/**")
                .uri(espaciosUrl))
            .route("reservas", r -> r
                .path("/api/reservas/**")
                .uri(reservasUrl))
            .build();
    }
}
