package com.yaku.repository;

import com.yaku.model.RegistroAsistencia;

import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a datos de asistencias (patron Repository).
 *
 * Punto donde se aplica la estrategia de persistencia (MySQL o memoria).
 * Solo lee y escribe registros: ni reglas de negocio ni salida por consola.
 */
public interface IAsistenciaRepository {

    /** Persiste un registro de asistencia y le asigna su id generado. */
    void guardar(RegistroAsistencia asistencia);

    /** Devuelve las asistencias de la fecha indicada, ordenadas por hora. */
    List<RegistroAsistencia> listarPorFecha(LocalDate fecha);
}
