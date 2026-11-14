package MicReservas.service;

import MicReservas.Reserva;
import MicReservas.dto.CrearReservaRequest;
import MicReservas.exception.ReglaNegocioException;
import MicReservas.model.EstadoReserva;
import MicReservas.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

   
                                       
                                                                                  
   
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {
                                                                                     
    @Mock ReservaRepository repository;
    ReservaService service;
    MockRestServiceServer usuariosServer;
    MockRestServiceServer espaciosServer;

                                                                                  
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 4, 8, 0);

                                                                   
    @BeforeEach void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        RestClient.Builder usuariosBuilder = RestClient.builder().baseUrl("http://usuarios-test");
        RestClient.Builder espaciosBuilder = RestClient.builder().baseUrl("http://espacios-test");
        usuariosServer = MockRestServiceServer.bindTo(usuariosBuilder).build();
        espaciosServer = MockRestServiceServer.bindTo(espaciosBuilder).build();
        service = new ReservaService(repository, clock, usuariosBuilder.build(), espaciosBuilder.build());
    }

                                                                                                         
    private void validarUsuarioYEspacioRemotos(int veces) { validarUsuarioYEspacioRemotos(veces, "LABORATORIO"); }

    private void validarUsuarioYEspacioRemotos(int veces, String tipoEspacio) {
        for (int i = 0; i < veces; i++) {
            usuariosServer.expect(requestTo("http://usuarios-test/api/usuarios/1"))
                    .andExpect(method(org.springframework.http.HttpMethod.GET))
                    .andRespond(withSuccess("{\"id\":1,\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"rol\":\"ESTUDIANTE\"}", MediaType.APPLICATION_JSON));
            espaciosServer.expect(requestTo("http://espacios-test/api/v1/espacios/10"))
                    .andExpect(method(org.springframework.http.HttpMethod.GET))
                    .andRespond(withSuccess("{\"id\":10,\"tipoEspacio\":\"" + tipoEspacio + "\",\"capacidadMax\":12,\"estado\":\"ACTIVO\"}", MediaType.APPLICATION_JSON));
        }
    }

                                                      
    @Test void rechazaReservaConMenosDeTresHoras() {
        LocalDateTime inicio = AHORA.plusHours(2);
        var request = new CrearReservaRequest(1, 10, 5, inicio);
        validarUsuarioYEspacioRemotos(1);
        assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

                                                                       
    @Test void rechazaAulaGeneralConMenosDeCincoEstudiantes() {
        LocalDateTime inicio = AHORA.plusHours(5);
        var request = new CrearReservaRequest(1, 10, 4, inicio);
        validarUsuarioYEspacioRemotos(1, "AULA_COMUN");
        assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

                                                                
    @Test void rechazaCuartaReservaActivaDelUsuario() {
        LocalDateTime inicio = AHORA.plusHours(5);
        var request = new CrearReservaRequest(1, 10, 5, inicio);
        when(repository.countByUsuarioIdAndEstado(1, EstadoReserva.ACTIVA)).thenReturn(3L);
        validarUsuarioYEspacioRemotos(1);
        assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

                                                                     
    @Test void rechazaReservaQueSeSuperponeConOtraActiva() {
        LocalDateTime inicio = AHORA.plusHours(5);
        var request = new CrearReservaRequest(1, 10, 5, inicio);
        when(repository.countByUsuarioIdAndEstado(1, EstadoReserva.ACTIVA)).thenReturn(0L);
        when(repository.existeSuperposicion(eq(10), eq(inicio), eq(inicio.plusHours(1)),
                eq(EstadoReserva.ACTIVA), isNull())).thenReturn(true);
        validarUsuarioYEspacioRemotos(1);
        assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

                                                                        
    @Test void rechazaEstudianteQueIntentaReservarConMasDe24Horas() {
        LocalDateTime inicio = AHORA.plusHours(33);                                                
        var request = new CrearReservaRequest(1, 10, 5, inicio);
        validarUsuarioYEspacioRemotos(1);
        assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

                                                                            
    @Test void permiteCancelarExactamenteSeisHorasAntes() {
        LocalDateTime inicio = AHORA.plusHours(6);
        Reserva existente = new Reserva(1, "Ana", "ESTUDIANTE", 10,
                "Laboratorio", 4, inicio, inicio.plusHours(1));
        when(repository.findById(44L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        Reserva cancelada = service.cancelar(44L);
        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
    }

    @Test void rechazaCancelarConMenosDeSeisHoras() {
        LocalDateTime inicio = AHORA.plusHours(5);
        Reserva existente = new Reserva(1, "Ana", "ESTUDIANTE", 10,
                "Laboratorio", 4, inicio, inicio.plusHours(1));
        when(repository.findById(44L)).thenReturn(Optional.of(existente));
        assertThrows(ReglaNegocioException.class, () -> service.cancelar(44L));
        verify(repository, never()).save(any());
    }

    @Test void permiteDosReservasConsecutivasDelMismoUsuarioEnElMismoEspacio() {
        LocalDateTime primeraHora = AHORA.plusHours(3);                 
        LocalDateTime segundaHora = primeraHora.plusHours(1);                 

        var primeraRequest = new CrearReservaRequest(1, 10, 5, primeraHora);
        var segundaRequest = new CrearReservaRequest(1, 10, 5, segundaHora);

                                                                          
        when(repository.countByUsuarioIdAndEstado(1, EstadoReserva.ACTIVA)).thenReturn(0L, 1L);
        when(repository.existeSuperposicion(eq(10), any(LocalDateTime.class),
                any(LocalDateTime.class), eq(EstadoReserva.ACTIVA), isNull())).thenReturn(false);
        when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        validarUsuarioYEspacioRemotos(2);
        Reserva primera = service.crear(primeraRequest);
        Reserva segunda = service.crear(segundaRequest);

        assertEquals(primeraHora, primera.getFechaHoraInicio());
        assertEquals(primeraHora.plusHours(1), primera.getFechaHoraFin());
        assertEquals(segundaHora, segunda.getFechaHoraInicio());
        assertEquals(segundaHora.plusHours(1), segunda.getFechaHoraFin());
        verify(repository, times(2)).save(any(Reserva.class));
    }

    @Test void permiteCrearReservaValida() {
        LocalDateTime inicio = AHORA.plusHours(5);
        var request = new CrearReservaRequest(1, 10, 5, inicio);
        when(repository.countByUsuarioIdAndEstado(1, EstadoReserva.ACTIVA)).thenReturn(0L);
        when(repository.existeSuperposicion(eq(10), eq(inicio), eq(inicio.plusHours(1)),
                eq(EstadoReserva.ACTIVA), isNull())).thenReturn(false);
        when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        validarUsuarioYEspacioRemotos(1);
        Reserva creada = service.crear(request);
        assertEquals(EstadoReserva.ACTIVA, creada.getEstado());
        assertEquals(inicio.plusHours(1), creada.getFechaHoraFin());
    }
    @Test void rechazaUsuarioInexistenteConErrorDeValidacion() {
        LocalDateTime inicio = AHORA.plusHours(5);
        var request = new CrearReservaRequest(999, "Ana", "ESTUDIANTE", 10,
                "LABORATORIO", 5, inicio);
        usuariosServer.expect(requestTo("http://usuarios-test/api/usuarios/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(repository, never()).save(any());
    }

    @Test void rechazaEspacioInexistenteConErrorDeValidacion() {
        LocalDateTime inicio = AHORA.plusHours(5);
        var request = new CrearReservaRequest(1, "Ana", "ESTUDIANTE", 999,
                "LABORATORIO", 5, inicio);
        usuariosServer.expect(requestTo("http://usuarios-test/api/usuarios/1"))
                .andRespond(withSuccess("{\"id\":1,\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"rol\":\"ESTUDIANTE\"}", MediaType.APPLICATION_JSON));
        espaciosServer.expect(requestTo("http://espacios-test/api/v1/espacios/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.crear(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(repository, never()).save(any());
    }


    @Test void cancelaTodasLasReservasActivasDeUnUsuarioYConservaLasHistoricas() {
        LocalDateTime inicio = AHORA.plusDays(1);
        Reserva activa1 = new Reserva(1, "Ana", "ESTUDIANTE", 10, "LABORATORIO", 5,
                inicio, inicio.plusHours(1));
        Reserva activa2 = new Reserva(1, "Ana", "ESTUDIANTE", 11, "LABORATORIO", 5,
                inicio.plusHours(2), inicio.plusHours(3));
        when(repository.findByUsuarioIdAndEstado(1, EstadoReserva.ACTIVA))
                .thenReturn(java.util.List.of(activa1, activa2));

        service.cancelarActivasDeUsuario(1);

        assertEquals(EstadoReserva.CANCELADA, activa1.getEstado());
        assertEquals(EstadoReserva.CANCELADA, activa2.getEstado());
        verify(repository, times(2)).save(any(Reserva.class));
    }

    @Test void cancelaTodasLasReservasActivasDeUnEspacio() {
        LocalDateTime inicio = AHORA.plusDays(1);
        Reserva activa = new Reserva(1, "Ana", "ESTUDIANTE", 10, "LABORATORIO", 5,
                inicio, inicio.plusHours(1));
        when(repository.findByEspacioIdAndEstado(10, EstadoReserva.ACTIVA))
                .thenReturn(java.util.List.of(activa));

        service.cancelarActivasDeEspacio(10);

        assertEquals(EstadoReserva.CANCELADA, activa.getEstado());
        verify(repository).save(activa);
    }

    @Test void rechazaCancelacionMasivaConIdentificadorNoPositivo() {
        assertThrows(ReglaNegocioException.class, () -> service.cancelarActivasDeUsuario(0));
        assertThrows(ReglaNegocioException.class, () -> service.cancelarActivasDeEspacio(-1));
        verify(repository, never()).findByUsuarioIdAndEstado(anyInt(), any());
        verify(repository, never()).findByEspacioIdAndEstado(anyInt(), any());
    }
}
