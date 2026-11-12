package MicReservas.repository;

import MicReservas.Reserva;
import MicReservas.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


public interface ReservaRepository extends JpaRepository<Reserva, Long> {
                                                                                     
                                                                             
    long countByUsuarioIdAndEstado(Integer usuarioId, EstadoReserva estado);

    List<Reserva> findByUsuarioIdAndEstado(Integer usuarioId, EstadoReserva estado);

    List<Reserva> findByEspacioIdAndEstado(Integer espacioId, EstadoReserva estado);

                                                                                                                                                              
    @Query("""
        select count(r) > 0 from Reserva r
        where r.espacioId = :espacioId
          and r.estado = :estado
          and r.fechaHoraInicio < :fin
          and r.fechaHoraFin > :inicio
          and (:excluirId is null or r.id <> :excluirId)
        """)
    boolean existeSuperposicion(@Param("espacioId") Integer espacioId,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin,
                                @Param("estado") EstadoReserva estado,
                                @Param("excluirId") Long excluirId);
                                            
    @Query("""
        select r from Reserva r
        where (:usuarioId is null or r.usuarioId = :usuarioId)
          and (:espacioId is null or r.espacioId = :espacioId)
          and (:estado is null or r.estado = :estado)
          and (:desde is null or r.fechaHoraInicio >= :desde)
          and (:hasta is null or r.fechaHoraInicio < :hasta)
        order by r.fechaHoraInicio asc
        """)
    List<Reserva> buscarConFiltros(@Param("usuarioId") Integer usuarioId,
                                   @Param("espacioId") Integer espacioId,
                                   @Param("estado") EstadoReserva estado,
                                   @Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta);

}
