package com.utn.espacios;

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
    "spring.datasource.url=jdbc:h2:mem:espacios_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "spring.h2.console.enabled=false"
})
class EspacioApiValidationTest {
    @Autowired MockMvc mvc;

    @Test void creaEspacioValido() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula 101","tipoEspacio":"AULA_COMUN","capacidadMax":30,
                     "tieneProyector":true,"esLaboratorio":false,"ubicacion":"Pabellon A"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Aula 101"))
            .andExpect(jsonPath("$.capacidadMax").value(30));
    }

    @Test void rechazaNombreVacio() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"  ","tipoEspacio":"AULA_COMUN","capacidadMax":30}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaCapacidadCero() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":"AULA_COMUN","capacidadMax":0}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaCapacidadNegativa() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":"AULA_COMUN","capacidadMax":-5}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaCapacidadConLetras() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":"AULA_COMUN","capacidadMax":"muchos"}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaTipoDeEspacioDesconocido() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":"AUDITORIO_GIGANTE","capacidadMax":30}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaTipoNulo() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":null,"capacidadMax":30}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaCapacidadDemasiadoGrande() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":"AULA_COMUN","capacidadMax":501}
                    """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaCantidadDeBusquedaNegativa() throws Exception {
        mvc.perform(get("/api/v1/espacios/buscar").param("cantidadPersonas", "-2"))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCantidadDeBusquedaConLetras() throws Exception {
        mvc.perform(get("/api/v1/espacios/buscar").param("cantidadPersonas", "abc"))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaIdConLetras() throws Exception {
        mvc.perform(get("/api/v1/espacios/abc")).andExpect(status().isBadRequest());
    }

    @Test void devuelve404ParaEspacioInexistente() throws Exception {
        mvc.perform(get("/api/v1/espacios/999999")).andExpect(status().isNotFound());
    }
    @Test void rechazaNombreNulo() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":null,"tipoEspacio":"AULA_COMUN","capacidadMax":30}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCapacidadNula() throws Exception {
        mvc.perform(post("/api/v1/espacios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Aula","tipoEspacio":"AULA_COMUN","capacidadMax":null}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaIdCeroONegativo() throws Exception {
        mvc.perform(get("/api/v1/espacios/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/espacios/-1")).andExpect(status().isBadRequest());
    }



}
