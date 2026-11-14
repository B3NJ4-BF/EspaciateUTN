package MicReservas.service;

import MicReservas.Reserva;
import MicReservas.dto.CrearReservaRequest;
import MicReservas.dto.ModificarReservaRequest;
import MicReservas.exception.ReglaNegocioException;
import MicReservas.exception.RecursoNoEncontradoException;
import MicReservas.model.EstadoReserva;
import MicReservas.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import java.time.Duration;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

   
                                                     
                                                                                      
   
@Service
public class ReservaService {
                                                                        
                                                                                      
    private static final int DURACION_RESERVA_HORAS = 1;
    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE = LocalTime.of(22, 0);
                                                                              
                                                             
    private static final ConcurrentHashMap<Integer, ReentrantLock> BLOQUEOS_ESPACIO = new ConcurrentHashMap<>();
    private final ReservaRepository repository;
    private final Clock clock;
    private final RestClient usuarioClient;
    private final RestClient espacioClient;
    private final GoogleCalendarService googleCalendarService;

    @Autowired
    public ReservaService(ReservaRepository repository, Clock clock,
                         @Value("${usuarios.service.url:http://localhost:8082}") String usuariosUrl,
                         @Value("${espacios.service.url:http://localhost:8083}") String espaciosUrl,
                         GoogleCalendarService googleCalendarService) {
        this.repository = repository;
        this.clock = clock;
        this.usuarioClient = RestClient.builder().baseUrl(usuariosUrl).build();
        this.espacioClient = RestClient.builder().baseUrl(espaciosUrl).build();
        this.googleCalendarService = googleCalendarService;
    }

                                                                                                 
    ReservaService(ReservaRepository repository, Clock clock, RestClient usuarioClient, RestClient espacioClient) {
        this.repository = repository;
        this.clock = clock;
        this.usuarioClient = usuarioClient;
        this.espacioClient = espacioClient;
        this.googleCalendarService = null;
    }

                                                             
    public ReservaService(ReservaRepository repository, Clock clock) {
        this(repository, clock, "http://localhost:8082", "http://localhost:8083", null);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
       
                                                                                
                                                                               
       
    public Reserva crear(CrearReservaRequest request, Integer usuarioAutenticadoId, boolean administrador) {
        Integer usuarioId = request.usuarioId();
        if (!administrador) {
            if (usuarioAutenticadoId == null) {
                throw new ReglaNegocioException("El token no contiene un usuario_id válido.", HttpStatus.FORBIDDEN);
            }
            if (usuarioId != null && !usuarioId.equals(usuarioAutenticadoId)) {
                throw new ReglaNegocioException("No puede crear una reserva a nombre de otro usuario.", HttpStatus.FORBIDDEN);
            }
            usuarioId = usuarioAutenticadoId;
        } else if (usuarioId == null) {
            usuarioId = usuarioAutenticadoId;
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new ReglaNegocioException("No se pudo determinar el usuario titular de la reserva.", HttpStatus.BAD_REQUEST);
        }

        ReentrantLock lock = BLOQUEOS_ESPACIO.computeIfAbsent(request.espacioId(), ignored -> new ReentrantLock());
        lock.lock();
        try {
            String rol = validarUsuarioExiste(usuarioId, request.rolUsuario());
            validarEspacio(request.espacioId(), request.tipoEspacio(), request.cantidadEstudiantes());
            LocalDateTime inicio = request.fechaHoraInicio();
            LocalDateTime fin = inicio.plusHours(DURACION_RESERVA_HORAS);
            validarDatosDeReserva(rol, request.tipoEspacio(), request.cantidadEstudiantes(), inicio, LocalDateTime.now(clock));
            validarLimiteUsuario(usuarioId);
            validarDisponibilidad(request.espacioId(), inicio, fin, null);

            Reserva reserva = new Reserva(usuarioId, request.nombreUsuario().trim(), rol,
                    request.espacioId(), request.tipoEspacio().trim(), request.cantidadEstudiantes(), inicio, fin);
            Reserva guardada = repository.save(reserva);
            if (googleCalendarService != null) {
                try {
                    guardada.setGoogleCalendarEventId(googleCalendarService.crearEvento(guardada));
                    guardada = repository.save(guardada);
                } catch (RuntimeException ex) {
                    org.slf4j.LoggerFactory.getLogger(ReservaService.class)
                            .error("Reserva {} creada localmente, pero falló Google Calendar", guardada.getId(), ex);
                }
            }
            return guardada;
        } finally {
            lock.unlock();
        }
    }

                                                                         
    public Reserva crear(CrearReservaRequest request) {
        return crear(request, request.usuarioId(), true);
    }

                                                                                     
    @Transactional(readOnly = true)
    public Reserva buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva con ID " + id + "."));
    }

    @Transactional(readOnly = true)
                                                                                       
    public List<Reserva> listar(Integer usuarioId, Integer espacioId, EstadoReserva estado,
                                LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha 'hasta' no puede ser anterior a 'desde'.", HttpStatus.BAD_REQUEST);
        }
                                                                                    
                                                                                       
        LocalDateTime desdeDt = desde == null ? null : desde.atStartOfDay();
        LocalDateTime hastaDt = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        return repository.buscarConFiltros(usuarioId, espacioId, estado, desdeDt, hastaDt);
    }

                                                                                 
    public Reserva modificar(Long id, ModificarReservaRequest request) {
        return modificar(id, request, null, true);
    }

    @Transactional
       
                                                                                            
                                                                                          
       
    public Reserva modificar(Long id, ModificarReservaRequest request, Integer usuarioAutenticadoId, boolean administrador) {
        Reserva reserva = buscarPorId(id);
        validarTitularidad(reserva, usuarioAutenticadoId, administrador);
        exigirActiva(reserva);
        LocalDateTime inicio = request.fechaHoraInicio();
        LocalDateTime fin = inicio.plusHours(DURACION_RESERVA_HORAS);
        validarEspacio(reserva.getEspacioId(), request.tipoEspacio(), request.cantidadEstudiantes());
        validarDatosDeReserva(reserva.getRolUsuario(), request.tipoEspacio(),
                request.cantidadEstudiantes(), inicio, LocalDateTime.now(clock));
                                                                              
        validarDisponibilidad(reserva.getEspacioId(), inicio, fin, reserva.getId());
        reserva.modificar(request.tipoEspacio().trim(), request.cantidadEstudiantes(), inicio, fin);
        Reserva guardada = repository.save(reserva);
        if (googleCalendarService != null) {
            try { googleCalendarService.actualizarEvento(guardada); }
            catch (RuntimeException ex) {
                org.slf4j.LoggerFactory.getLogger(ReservaService.class)
                        .error("Reserva {} modificada localmente, pero falló Google Calendar", guardada.getId(), ex);
            }
        }
        return guardada;
    }

                                                                                 
    public Reserva cancelar(Long id) {
        return cancelar(id, null, true);
    }

    @Transactional
                                                                                  
    public Reserva cancelar(Long id, Integer usuarioAutenticadoId, boolean administrador) {
        Reserva reserva = buscarPorId(id);
        validarTitularidad(reserva, usuarioAutenticadoId, administrador);
        exigirActiva(reserva);
                                                                                              
        if (Duration.between(LocalDateTime.now(clock), reserva.getFechaHoraInicio()).compareTo(Duration.ofHours(6)) < 0) {
            throw new ReglaNegocioException("La cancelación debe realizarse con al menos 6 horas de anticipación.");
        }
        reserva.cancelar();
        Reserva guardada = repository.save(reserva);
        if (googleCalendarService != null) {
            try { googleCalendarService.cancelarEvento(guardada); }
            catch (RuntimeException ex) {
                org.slf4j.LoggerFactory.getLogger(ReservaService.class)
                        .error("Reserva {} cancelada localmente, pero falló Google Calendar", guardada.getId(), ex);
            }
        }
        return guardada;
    }

       
                                                                                        
                                                                                   
       
    private void validarDatosDeReserva(String rol, String tipoEspacio, Integer cantidad,
                                       LocalDateTime inicio, LocalDateTime ahora) {
        if (inicio == null) throw new ReglaNegocioException("La fecha y hora de inicio son obligatorias.", HttpStatus.BAD_REQUEST);
        if (inicio.getMinute() != 0 || inicio.getSecond() != 0 || inicio.getNano() != 0) {
            throw new ReglaNegocioException("Las reservas deben comenzar en una hora en punto (por ejemplo, 10:00).", HttpStatus.BAD_REQUEST);
        }
        LocalTime hora = inicio.toLocalTime();
        LocalDateTime fin = inicio.plusHours(DURACION_RESERVA_HORAS);
        if (hora.isBefore(APERTURA) || fin.toLocalTime().isAfter(CIERRE)) {
            throw new ReglaNegocioException("El horario de atención es de 08:00 a 22:00 hs.", HttpStatus.BAD_REQUEST);
        }
        if (inicio.toLocalDate().isBefore(ahora.toLocalDate()) || inicio.isBefore(ahora)) {
            throw new ReglaNegocioException("No se puede reservar una fecha u horario pasado.", HttpStatus.BAD_REQUEST);
        }
                                                                                 
                                                                                   
        Duration anticipacion = Duration.between(ahora, inicio);
        if (anticipacion.compareTo(Duration.ofHours(3)) < 0) {
            throw new ReglaNegocioException("Las reservas deben realizarse con al menos 3 horas de anticipación.");
        }
                                                                            
        long maximo = rol.equals("ESTUDIANTE") ? 24 : 48;
        if (anticipacion.compareTo(Duration.ofHours(maximo)) > 0) {
            throw new ReglaNegocioException("El máximo de anticipación para " + rol.toLowerCase(Locale.ROOT)
                    + " es de " + maximo + " horas.");
        }
        String tipo = tipoEspacio == null ? "" : tipoEspacio.toLowerCase(Locale.ROOT);
        if ((tipo.contains("aula general") || tipo.contains("proyector")) && cantidad < 5) {
            throw new ReglaNegocioException("Las aulas generales y las aulas con proyector requieren un mínimo de 5 estudiantes.", HttpStatus.BAD_REQUEST);
        }
    }

                                                                                                       
    private String validarUsuarioExiste(Integer usuarioId, String rolInformado) {
        try {
            UsuarioRemoto usuario = usuarioClient.get().uri("/api/usuarios/{id}", usuarioId)
                    .retrieve().body(UsuarioRemoto.class);
            if (usuario == null || usuario.id() == null)
                throw new ReglaNegocioException("El usuario indicado no existe.", HttpStatus.BAD_REQUEST);
                                                                                
            return normalizarRol(usuario.rol());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ReglaNegocioException("El usuario indicado no existe.", HttpStatus.BAD_REQUEST);
        } catch (RestClientException ex) {
            throw new ReglaNegocioException("No se pudo validar el usuario en Usuarios. Verifique que el servicio esté disponible.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

                                                                                          
    private void validarEspacio(Integer espacioId, String tipoSolicitado, Integer cantidad) {
        try {
            EspacioRemoto espacio = espacioClient.get().uri("/api/v1/espacios/{id}", espacioId)
                    .retrieve().body(EspacioRemoto.class);
            if (espacio == null || espacio.id() == null)
                throw new ReglaNegocioException("El espacio indicado no existe.", HttpStatus.BAD_REQUEST);
            if (!"ACTIVO".equalsIgnoreCase(espacio.estado()))
                throw new ReglaNegocioException("El espacio no está ACTIVO y no puede reservarse.", HttpStatus.BAD_REQUEST);
            if (espacio.capacidadMax() == null || cantidad == null || cantidad < 1
                    || cantidad > espacio.capacidadMax())
                throw new ReglaNegocioException("La cantidad de estudiantes supera la capacidad máxima del espacio.", HttpStatus.BAD_REQUEST);
            String tipoCatalogo = espacio.tipoEspacio() == null ? "" : espacio.tipoEspacio().toUpperCase(Locale.ROOT);
            String tipoReserva = tipoSolicitado == null ? "" : tipoSolicitado.toUpperCase(Locale.ROOT);
            if (!tipoCatalogo.equals(tipoReserva))
                throw new ReglaNegocioException("El tipo de espacio informado no coincide con el catálogo.", HttpStatus.BAD_REQUEST);
            if ("BOX_ESTUDIO".equals(tipoCatalogo) && cantidad > 4)
                throw new ReglaNegocioException("BOX_ESTUDIO admite hasta 4 personas.", HttpStatus.BAD_REQUEST);
            if (!"BOX_ESTUDIO".equals(tipoCatalogo) && cantidad < 5)
                throw new ReglaNegocioException("AULA_COMUN y LABORATORIO requieren al menos 5 personas.", HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ReglaNegocioException("El espacio indicado no existe.", HttpStatus.BAD_REQUEST);
        } catch (RestClientException ex) {
            throw new ReglaNegocioException("No se pudo validar el espacio en Espacios. Verifique que el servicio esté disponible.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record UsuarioRemoto(Long id, String rol) {}
    private record EspacioRemoto(Long id, String tipoEspacio, Integer capacidadMax, String estado) {}

                                                                                     
    private String normalizarRol(String rol) {
        String normalizado = rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
        if (normalizado.equals("ADMIN")) normalizado = "ADMINISTRADOR";
        if (!normalizado.equals("DOCENTE") && !normalizado.equals("ESTUDIANTE")
                && !normalizado.equals("ADMINISTRADOR")) {
            throw new ReglaNegocioException("El rol debe ser DOCENTE, ESTUDIANTE o ADMINISTRADOR.", HttpStatus.BAD_REQUEST);
        }
        return normalizado;
    }

                                                                                 
    private void validarLimiteUsuario(Integer usuarioId) {
        long activas = repository.countByUsuarioIdAndEstado(usuarioId, EstadoReserva.ACTIVA);
        if (activas >= 3) throw new ReglaNegocioException("El usuario ya alcanzó el máximo de 3 reservas activas.");
    }

                                                                      
                                                      
    private void validarDisponibilidad(Integer espacioId, LocalDateTime inicio,
                                       LocalDateTime fin, Long excluirId) {
        boolean ocupada = repository.existeSuperposicion(espacioId, inicio, fin,
                EstadoReserva.ACTIVA, excluirId);
        if (ocupada) throw new ReglaNegocioException("El espacio ya tiene una reserva activa en ese horario.");
    }

                                                                                      
       
                                                                                   
                                                                                    
       
    /**
     * Cancela todas las reservas activas de un usuario. Se usa durante la eliminación
     * coordinada de usuarios; las reservas históricas se conservan.
     */
    @Transactional
    public void cancelarActivasDeUsuario(Integer usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new ReglaNegocioException("El usuarioId debe ser positivo.", HttpStatus.BAD_REQUEST);
        }
        List<Reserva> activas = repository.findByUsuarioIdAndEstado(usuarioId, EstadoReserva.ACTIVA);
        for (Reserva reserva : activas) {
            reserva.cancelar();
            repository.save(reserva);
            sincronizarCancelacionCalendar(reserva);
        }
    }

    /**
     * Cancela todas las reservas activas de un espacio antes de eliminarlo.
     */
    @Transactional
    public void cancelarActivasDeEspacio(Integer espacioId) {
        if (espacioId == null || espacioId <= 0) {
            throw new ReglaNegocioException("El espacioId debe ser positivo.", HttpStatus.BAD_REQUEST);
        }
        List<Reserva> activas = repository.findByEspacioIdAndEstado(espacioId, EstadoReserva.ACTIVA);
        for (Reserva reserva : activas) {
            reserva.cancelar();
            repository.save(reserva);
            sincronizarCancelacionCalendar(reserva);
        }
    }

    private void sincronizarCancelacionCalendar(Reserva reserva) {
        if (googleCalendarService == null) return;
        try {
            googleCalendarService.cancelarEvento(reserva);
        } catch (RuntimeException ex) {
            // La cancelación local no debe quedar bloqueada por una caída de Google.
            // El identificador se conserva para permitir reconciliarlo posteriormente.
            org.slf4j.LoggerFactory.getLogger(ReservaService.class)
                    .error("No se pudo sincronizar la cancelación de la reserva {} con Google Calendar",
                            reserva.getId(), ex);
        }
    }

    private void validarTitularidad(Reserva reserva, Integer usuarioAutenticadoId, boolean administrador) {
        if (administrador) return;
        if (usuarioAutenticadoId == null || !usuarioAutenticadoId.equals(reserva.getUsuarioId())) {
            throw new ReglaNegocioException("No está autorizado para modificar una reserva que no le pertenece.",
                    HttpStatus.FORBIDDEN);
        }
    }

    private void exigirActiva(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new ReglaNegocioException("La reserva ya está cancelada.");
        }
    }
}
