package com.utn.espacios.repository;

import com.utn.espacios.entity.Espacio;
import com.utn.espacios.entity.EstadoEspacio;
import com.utn.espacios.entity.TipoEspacio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    List<Espacio> findByEstado(EstadoEspacio estado);

    List<Espacio> findByTipoEspacio(TipoEspacio tipoEspacio);

    List<Espacio> findByTieneProyector(Boolean tieneProyector);

    List<Espacio> findByEsLaboratorio(Boolean esLaboratorio);

    List<Espacio> findByEstadoAndCapacidadMaxGreaterThanEqual(
            EstadoEspacio estado,
            Integer capacidadMinima);

    List<Espacio> findByEstadoAndTipoEspacio(EstadoEspacio estado, TipoEspacio tipoEspacio);
}
