package com.yaku.repository;

import com.yaku.model.RegistroAsistencia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion del repositorio de asistencias en memoria.
 * Estrategia de respaldo cuando no hay base de datos disponible.
 */
public class AsistenciaRepositoryMemoria implements IAsistenciaRepository {

    private final List<RegistroAsistencia> almacen = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void guardar(RegistroAsistencia a) {
        a.setId(nextId++);
        almacen.add(a);
    }

    @Override
    public List<RegistroAsistencia> listarPorFecha(LocalDate fecha) {
        return almacen.stream()
                .filter(a -> a.getFecha().equals(fecha))
                .sorted((x, y) -> x.getHora().compareTo(y.getHora()))
                .toList();
    }
}
