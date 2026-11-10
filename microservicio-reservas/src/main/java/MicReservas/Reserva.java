package MicReservas;

import MicReservas.model.EstadoReserva;
import jakarta.persistence.*;
import java.time.LocalDateTime;

   
                                                                         
                                                                                     
   
@Entity
@Table(name = "reservas", indexes = {
        @Index(name = "idx_reserva_espacio_inicio", columnList = "espacio_id, fecha_hora_inicio"),
        @Index(name = "idx_reserva_usuario_estado", columnList = "usuario_id, estado")
})
public class Reserva {
                                                                                     
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

                                                                                
                                                                                 
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "nombre_usuario", nullable = false, length = 120)
    private String nombreUsuario;

    @Column(name = "rol_usuario", nullable = false, length = 20)
    private String rolUsuario;

                                                                                          
    @Column(name = "espacio_id", nullable = false)
    private Integer espacioId;

    @Column(name = "tipo_espacio", nullable = false, length = 60)
    private String tipoEspacio;

    @Column(name = "cantidad_estudiantes", nullable = false)
    private Integer cantidadEstudiantes;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

                                                                                  
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

                                                                                  
                                                                                     
                                                                                  
    @Column(name = "google_calendar_event_id", length = 255)
    private String googleCalendarEventId;

    @Version
    private Long version;

                                                                                       
                                                                                      
    protected Reserva() {}

                                                                                             
    public Reserva(Integer usuarioId, String nombreUsuario, String rolUsuario,
                   Integer espacioId, String tipoEspacio, Integer cantidadEstudiantes,
                   LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.rolUsuario = rolUsuario;
        this.espacioId = espacioId;
        this.tipoEspacio = tipoEspacio;
        this.cantidadEstudiantes = cantidadEstudiantes;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
                                                                                       
                                                                                     
        this.estado = EstadoReserva.ACTIVA;
    }

                                                                                
                                                          
    public void modificar(String tipoEspacio, Integer cantidadEstudiantes,
                          LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.tipoEspacio = tipoEspacio;
        this.cantidadEstudiantes = cantidadEstudiantes;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }

                                                                                      
    public void cancelar() { this.estado = EstadoReserva.CANCELADA; }

                                                                                   
                                                                                   
    public Long getId() { return id; }
    public Integer getUsuarioId() { return usuarioId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getRolUsuario() { return rolUsuario; }
    public Integer getEspacioId() { return espacioId; }
    public String getTipoEspacio() { return tipoEspacio; }
    public Integer getCantidadEstudiantes() { return cantidadEstudiantes; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public EstadoReserva getEstado() { return estado; }
    public String getGoogleCalendarEventId() { return googleCalendarEventId; }
    public void setGoogleCalendarEventId(String googleCalendarEventId) { this.googleCalendarEventId = googleCalendarEventId; }
    public Long getVersion() { return version; }
}
