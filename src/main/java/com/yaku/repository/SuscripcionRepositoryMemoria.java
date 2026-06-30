package com.yaku.repository;

import com.yaku.model.Suscripcion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del repositorio de suscripciones en memoria.
 * Estrategia de respaldo cuando no hay base de datos disponible.
 */
public class SuscripcionRepositoryMemoria implements ISuscripcionRepository {

    private final Map<String, Suscripcion> porEstudiante = new HashMap<>();
    private int nextId = 1;

    @Override
    public void guardar(Suscripcion s) {
        s.setId(nextId++);
        porEstudiante.put(s.getEstudianteId(), s);
    }

    @Override
    public Suscripcion buscarPorEstudiante(String estudianteId) {
        return porEstudiante.get(estudianteId);
    }

    @Override
    public List<Suscripcion> listarTodas() {
        return new ArrayList<>(porEstudiante.values());
    }

    @Override
    public void actualizar(Suscripcion s) {
        porEstudiante.put(s.getEstudianteId(), s);
    }
}
