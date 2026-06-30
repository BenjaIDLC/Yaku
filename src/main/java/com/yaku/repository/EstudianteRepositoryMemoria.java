package com.yaku.repository;

import com.yaku.model.Estudiante;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del repositorio de estudiantes en memoria.
 * Estrategia de respaldo cuando no hay base de datos disponible.
 */
public class EstudianteRepositoryMemoria implements IEstudianteRepository {

    private final Map<String, Estudiante> almacen = new LinkedHashMap<>();

    @Override
    public void guardar(Estudiante e) {
        almacen.put(e.getId(), e);
    }

    @Override
    public void actualizar(Estudiante e) {
        almacen.put(e.getId(), e);
    }

    @Override
    public void eliminar(String id) {
        almacen.remove(id);
    }

    @Override
    public Estudiante buscarPorId(String id) {
        return almacen.get(id);
    }

    @Override
    public List<Estudiante> listarTodos() {
        List<Estudiante> lista = new ArrayList<>(almacen.values());
        lista.sort(Comparator.comparing(Estudiante::getApellido).thenComparing(Estudiante::getNombre));
        return lista;
    }

    @Override
    public String obtenerUltimoId() {
        return almacen.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
