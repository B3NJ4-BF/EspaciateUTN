package com.utn.espacios.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EspacioTest {

    @Test
    void entreUnaYCuatroPersonasSoloSeAdmitenEnBox() {
        Espacio box = crearEspacio(TipoEspacio.BOX_ESTUDIO, 10);
        Espacio aula = crearEspacio(TipoEspacio.AULA_COMUN, 30);

        assertTrue(box.cumpleConCupoMinimo(1));
        assertTrue(box.cumpleConCupoMinimo(4));
        assertFalse(box.cumpleConCupoMinimo(5));
        assertFalse(aula.cumpleConCupoMinimo(4));
        assertTrue(aula.cumpleConCupoMinimo(5));
    }

    @Test
    void noAdmiteCantidadCeroNiSuperarLaCapacidad() {
        Espacio aula = crearEspacio(TipoEspacio.AULA_COMUN, 10);

        assertFalse(aula.cumpleConCupoMinimo(0));
        assertFalse(aula.cumpleConCupoMinimo(11));
    }

    @Test
    void validaRequerimientosDeEquipamiento() {
        Espacio laboratorio = crearEspacio(TipoEspacio.LABORATORIO, 20);
        laboratorio.setEstado(EstadoEspacio.ACTIVO);
        laboratorio.setTieneProyector(true);
        laboratorio.setEsLaboratorio(true);

        assertTrue(laboratorio.cumpleConRequerimientos(8, true, true));
        assertTrue(laboratorio.cumpleConRequerimientos(8, false, false));

        laboratorio.setTieneProyector(false);
        assertFalse(laboratorio.cumpleConRequerimientos(8, true, false));

        laboratorio.setEstado(EstadoEspacio.EN_MANTENIMIENTO);
        assertFalse(laboratorio.cumpleConRequerimientos(8, true, true));
    }

    private Espacio crearEspacio(TipoEspacio tipo, int capacidad) {
        Espacio espacio = new Espacio();
        espacio.setNombre("Espacio de prueba");
        espacio.setTipoEspacio(tipo);
        espacio.setCapacidadMax(capacidad);
        espacio.setEstado(EstadoEspacio.ACTIVO);
        return espacio;
    }
}
