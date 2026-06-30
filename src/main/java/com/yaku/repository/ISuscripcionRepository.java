package com.yaku.repository;

import com.yaku.model.Suscripcion;

import java.util.List;

/**
 * Acceso a datos de suscripciones (patron Repository).
 *
 * Cada estudiante tiene como mucho una suscripcion (su saldo de clases). Este es
 * el punto donde se aplica la estrategia de persistencia (MySQL/H2 o memoria);
 * no contiene reglas de negocio ni imprime nada.
 */
public interface ISuscripcionRepository {

    /** Persiste una suscripcion nueva y le asigna su id generado. */
    void guardar(Suscripcion suscripcion);

    /** Devuelve la suscripcion del estudiante, o null si no tiene. */
    Suscripcion buscarPorEstudiante(String estudianteId);

    /** Devuelve todas las suscripciones registradas. */
    List<Suscripcion> listarTodas();

    /** Persiste los cambios de saldo/estado de una suscripcion existente. */
    void actualizar(Suscripcion suscripcion);
}
