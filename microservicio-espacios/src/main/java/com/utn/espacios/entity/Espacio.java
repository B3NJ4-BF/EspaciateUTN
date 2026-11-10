package com.utn.espacios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "espacios")
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_espacio", nullable = false, length = 30)
    private TipoEspacio tipoEspacio;

    @NotNull
    @Min(1)
    @Column(name = "capacidad_max", nullable = false)
    private Integer capacidadMax;

    @Column(name = "tiene_proyector", nullable = false)
    private Boolean tieneProyector = false;

    @Column(name = "es_laboratorio", nullable = false)
    private Boolean esLaboratorio = false;

    @Column(length = 150)
    private String ubicacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEspacio estado = EstadoEspacio.ACTIVO;

    public Espacio() {
    }

    @PrePersist
    @PreUpdate
    private void aplicarValoresPorDefecto() {
        if (tieneProyector == null) {
            tieneProyector = false;
        }
        if (esLaboratorio == null) {
            esLaboratorio = false;
        }
        if (estado == null) {
            estado = EstadoEspacio.ACTIVO;
        }
    }

       
                                                                             
                                                                              
                                         
       
    public boolean cumpleConCupoMinimo(int cantidadPersonas) {
        if (cantidadPersonas <= 0 || capacidadMax == null || cantidadPersonas > capacidadMax
                || tipoEspacio == null) {
            return false;
        }

        if (cantidadPersonas <= 4) {
            return tipoEspacio == TipoEspacio.BOX_ESTUDIO;
        }

        return tipoEspacio != TipoEspacio.BOX_ESTUDIO;
    }

       
                                                                                  
                                                                                          
       
    public boolean cumpleConRequerimientos(
            int cantidadPersonas,
            Boolean requiereProyector,
            Boolean requiereLab) {

        if (estado != EstadoEspacio.ACTIVO || !cumpleConCupoMinimo(cantidadPersonas)) {
            return false;
        }

        boolean necesitaProyector = Boolean.TRUE.equals(requiereProyector);
        boolean necesitaLaboratorio = Boolean.TRUE.equals(requiereLab);

        return (!necesitaProyector || Boolean.TRUE.equals(tieneProyector))
                && (!necesitaLaboratorio || Boolean.TRUE.equals(esLaboratorio));
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoEspacio getTipoEspacio() {
        return tipoEspacio;
    }

    public void setTipoEspacio(TipoEspacio tipoEspacio) {
        this.tipoEspacio = tipoEspacio;
    }

    public Integer getCapacidadMax() {
        return capacidadMax;
    }

    public void setCapacidadMax(Integer capacidadMax) {
        this.capacidadMax = capacidadMax;
    }

    public Boolean getTieneProyector() {
        return tieneProyector;
    }

    public void setTieneProyector(Boolean tieneProyector) {
        this.tieneProyector = tieneProyector;
    }

    public Boolean getEsLaboratorio() {
        return esLaboratorio;
    }

    public void setEsLaboratorio(Boolean esLaboratorio) {
        this.esLaboratorio = esLaboratorio;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EstadoEspacio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEspacio estado) {
        this.estado = estado;
    }
}
