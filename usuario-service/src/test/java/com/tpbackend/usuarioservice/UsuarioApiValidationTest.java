package com.tpbackend.usuarioservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:usuarios_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.h2.console.enabled=false"
})
class UsuarioApiValidationTest {
    @Autowired MockMvc mvc;

    @Test void creaUsuarioValidoYNoDevuelvePassword() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"ana.perez@test.com",
                     "password":"ClaveSegura123","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("ana.perez@test.com"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test void rechazaNombreVacio() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"   ","apellido":"Perez","email":"ana@test.com",
                     "password":"ClaveSegura123","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaEmailSinFormato() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"esto-no-es-email",
                     "password":"ClaveSegura123","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaPasswordCorta() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"ana2@test.com",
                     "password":"123","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaRolInventado() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"ana3@test.com",
                     "password":"ClaveSegura123","rol":"SUPERADMIN"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCampoObligatorioAusente() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"ana4@test.com",
                     "password":"ClaveSegura123"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaEmailDuplicadoCon409() throws Exception {
        String body = """
            {"nombre":"Ana","apellido":"Perez","email":"duplicado@test.com",
             "password":"ClaveSegura123","rol":"ESTUDIANTE"}
            """;
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
    }

    @Test void rechazaIdConLetras() throws Exception {
        mvc.perform(get("/api/usuarios/abc")).andExpect(status().isBadRequest());
    }

    @Test void devuelve404ParaUsuarioInexistente() throws Exception {
        mvc.perform(get("/api/usuarios/999999")).andExpect(status().isNotFound());
    }
    @Test void rechazaApellidoVacio() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"  ","email":"ana.apellido@test.com",
                     "password":"ClaveSegura123","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaEmailNulo() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":null,
                     "password":"ClaveSegura123","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaPasswordEnBlanco() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"ana.password@test.com",
                     "password":"   ","rol":"ESTUDIANTE"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaRolVacio() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","apellido":"Perez","email":"ana.rol@test.com",
                     "password":"ClaveSegura123","rol":""}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaIdCeroONegativo() throws Exception {
        mvc.perform(get("/api/usuarios/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/usuarios/-2")).andExpect(status().isBadRequest());
    }

}
