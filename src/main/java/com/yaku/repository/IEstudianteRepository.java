package com.yaku.repository;

import com.yaku.model.Estudiante;

import java.util.List;

/**
 * Acceso a datos de estudiantes (patron Repository).
 *
 * Punto donde se aplica la estrategia de persistencia (MySQL o memoria).
 * Solo lee y escribe estudiantes: ni reglas de negocio ni salida por consola.
 */
public interface IEstudianteRepository {

    /** Persiste un estudiante (su id ya viene asignado por el servicio). */
    void guardar(Estudiante estudiante);

    /** Actualiza los datos de un estudiante existente (identificado por su id). */
    void actualizar(Estudiante estudiante);

    /** Elimina al estudiante con ese id. */
    void eliminar(String id);

    /** Devuelve el estudiante con ese id, o null si no existe. */
    Estudiante buscarPorId(String id);

    /** Devuelve todos los estudiantes, ordenados por apellido y nombre. */
    List<Estudiante> listarTodos();

    /** Devuelve el id mas alto registrado (ej: "EST007"), o null si no hay ninguno. */
    String obtenerUltimoId();
}
