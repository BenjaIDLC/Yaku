package com.yaku.service;

import com.yaku.model.Suscripcion;
import com.yaku.repository.ISuscripcionRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Reglas de negocio de las suscripciones (saldo de clases prepagadas).
 *
 * Cada estudiante tiene un unico saldo. Las clases se abonan por paquetes y se
 * acumulan; no vencen. Esta clase no sabe si los datos van a base de datos o a
 * memoria —depende solo de {@link ISuscripcionRepository}— y nunca imprime.
 */
public class SuscripcionService {

    /** Desenlaces posibles de una cancelacion; el texto lo pone la vista. */
    public enum ResultadoCancelacion { CANCELADA, NO_EXISTE, YA_CANCELADA }

    /** Umbral de clases por debajo del cual una suscripcion se considera con saldo bajo. */
    public static final int UMBRAL_SALDO_BAJO = 3;

    private final ISuscripcionRepository repositorio;

    public SuscripcionService(ISuscripcionRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Agrega clases al saldo del estudiante, creando la suscripcion si no existe
     * (o reactivandola si estaba agotada/cancelada). Devuelve la suscripcion actualizada.
     */
    public Suscripcion abonar(String estudianteId, int clases) {
        Suscripcion s = repositorio.buscarPorEstudiante(estudianteId);
        if (s == null) {
            s = new Suscripcion(0, estudianteId, clases, LocalDate.now(), "ACTIVA");
            repositorio.guardar(s);
        } else {
            s.setClasesRestantes(s.getClasesRestantes() + clases);
            s.setEstado("ACTIVA");
            repositorio.actualizar(s);
        }
        return s;
    }

    /** Suscripcion del estudiante en cualquier estado, o null si no tiene. */
    public Suscripcion obtenerSuscripcion(String estudianteId) {
        return repositorio.buscarPorEstudiante(estudianteId);
    }

    /** Suscripcion ACTIVA con clases disponibles, o null. */
    public Suscripcion obtenerActiva(String estudianteId) {
        Suscripcion s = repositorio.buscarPorEstudiante(estudianteId);
        return (s != null && s.getEstado().equals("ACTIVA") && s.getClasesRestantes() > 0) ? s : null;
    }

    /**
     * Consume una clase de la suscripcion y la persiste.
     * @return true si la suscripcion quedo AGOTADA tras el consumo.
     */
    public boolean registrarUsoClase(Suscripcion activa) {
        int nuevas = activa.getClasesRestantes() - 1;
        activa.setClasesRestantes(nuevas);
        boolean agotada = nuevas <= 0;
        if (agotada) {
            activa.setEstado("AGOTADA");
        }
        repositorio.actualizar(activa);
        return agotada;
    }

    /**
     * Suscripciones con saldo bajo que conviene renovar: las no canceladas con
     * {@code clasesRestantes <= UMBRAL_SALDO_BAJO} (incluye las AGOTADA),
     * ordenadas de menor a mayor saldo.
     */
    public List<Suscripcion> listarConSaldoBajo() {
        return repositorio.listarTodas().stream()
                .filter(s -> !s.getEstado().equals("CANCELADA"))
                .filter(s -> s.getClasesRestantes() <= UMBRAL_SALDO_BAJO)
                .sorted(Comparator.comparingInt(Suscripcion::getClasesRestantes))
                .toList();
    }

    /** Cancela la suscripcion del estudiante y anula su saldo de clases. */
    public ResultadoCancelacion cancelar(String estudianteId) {
        Suscripcion s = repositorio.buscarPorEstudiante(estudianteId);
        if (s == null) {
            return ResultadoCancelacion.NO_EXISTE;
        }
        if (s.getEstado().equals("CANCELADA")) {
            return ResultadoCancelacion.YA_CANCELADA;
        }
        s.setEstado("CANCELADA");
        s.setClasesRestantes(0);
        repositorio.actualizar(s);
        return ResultadoCancelacion.CANCELADA;
    }
}
