package MicReservas;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.DELETE, "/api/reservas/interno/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/reservas").hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")
                    .requestMatchers(HttpMethod.PUT, "/api/reservas/*").hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")
                    .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/cancelacion").hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")
                    .requestMatchers(HttpMethod.GET, "/api/reservas", "/api/reservas/*").hasRole("ADMIN")
                    .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter())))
                .build();
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> converter() {
        return jwt -> {
            Map<String, List<String>> realm = jwt.getClaim("realm_access");
            List<SimpleGrantedAuthority> authorities = (realm == null ? List.<String>of()
                    : realm.getOrDefault("roles", List.of())).stream()
                    .map(r -> r.toUpperCase(Locale.ROOT))
                    .map(r -> "ADMINISTRADOR".equals(r) ? "ADMIN" : r)
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}

