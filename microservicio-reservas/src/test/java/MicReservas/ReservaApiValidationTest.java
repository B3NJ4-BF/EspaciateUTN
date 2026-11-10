package MicReservas;

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
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:reservas_api_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.h2.console.enabled=false",
    "google.calendar.enabled=false"
})
class ReservaApiValidationTest {
    @Autowired MockMvc mvc;

    private String json(String usuarioId, String espacioId, String cantidad, String rol, String tipo) {
        return """
            {"usuarioId":%s,"nombreUsuario":"Ana","rolUsuario":"%s","espacioId":%s,
             "tipoEspacio":"%s","cantidadEstudiantes":%s,"fechaHoraInicio":"2026-11-10T14:00:00"}
            """.formatted(usuarioId, rol, espacioId, tipo, cantidad);
    }

    @Test void rechazaUsuarioIdNegativo() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("-1", "10", "5", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaEspacioIdCero() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "0", "5", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCantidadNegativa() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "-3", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCantidadConLetras() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "\"cinco\"", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaRolInventado() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "5", "INVITADO", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaTipoEspacioVacio() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "5", "ESTUDIANTE", "")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaTipoEspacioInexistente() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "5", "ESTUDIANTE", "CANCHA")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaNombreVacio() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content("""
              {"usuarioId":1,"nombreUsuario":" ","rolUsuario":"ESTUDIANTE","espacioId":10,
               "tipoEspacio":"LABORATORIO","cantidadEstudiantes":5,"fechaHoraInicio":"2026-11-10T14:00:00"}
              """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaFechaConFormatoInvalido() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content("""
              {"usuarioId":1,"nombreUsuario":"Ana","rolUsuario":"ESTUDIANTE","espacioId":10,
               "tipoEspacio":"LABORATORIO","cantidadEstudiantes":5,"fechaHoraInicio":"mañana"}
              """)).andExpect(status().isBadRequest());
    }

    @Test void rechazaIdDeRutaConLetras() throws Exception {
        mvc.perform(get("/api/reservas/abc")).andExpect(status().isBadRequest());
    }

    @Test void rechazaEstadoDeFiltroDesconocido() throws Exception {
        mvc.perform(get("/api/reservas").param("estado", "PENDIENTE"))
            .andExpect(status().isBadRequest());
    }

    @Test void rutaDeConsultaDeAulasOcupadasYaNoEsUnEndpoint() throws Exception {
        mvc.perform(get("/api/reservas/disponibilidad")
                .param("espacioId", "10").param("fecha", "2026-11-10"))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCantidadCero() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "0", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaUsuarioIdConLetras() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("\"uno\"", "10", "5", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaNombreNulo() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content("""
              {"usuarioId":1,"nombreUsuario":null,"rolUsuario":"ESTUDIANTE","espacioId":10,
               "tipoEspacio":"LABORATORIO","cantidadEstudiantes":5,"fechaHoraInicio":"2026-11-10T14:00:00"}
              """))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaRolVacio() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "5", "", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaCantidadMayorAlLimite() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content(json("1", "10", "501", "ESTUDIANTE", "LABORATORIO")))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaIdCeroONegativoEnRuta() throws Exception {
        mvc.perform(get("/api/reservas/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reservas/-1")).andExpect(status().isBadRequest());
    }

    @Test void rechazaFiltroDeEspacioNegativo() throws Exception {
        mvc.perform(get("/api/reservas").param("espacioId", "-3"))
            .andExpect(status().isBadRequest());
    }

    @Test void rechazaFechaConHoraEnFormatoIncorrecto() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
            .content("""
              {"usuarioId":1,"nombreUsuario":"Ana","rolUsuario":"ESTUDIANTE","espacioId":10,
               "tipoEspacio":"LABORATORIO","cantidadEstudiantes":5,"fechaHoraInicio":"2026/11/10 14:00"}
              """))
            .andExpect(status().isBadRequest());
    }

}
