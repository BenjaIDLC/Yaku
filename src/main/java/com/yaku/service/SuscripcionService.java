package com.yaku.service;

import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Suscripcion;
import com.yaku.repository.ISuscripcionRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    private final MaquinaEstadosSuscripcion maquina = new MaquinaEstadosSuscripcion();

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
            s = new Suscripcion(0, estudianteId, clases, LocalDate.now(), EstadoSuscripcion.ACTIVA);
            repositorio.guardar(s);
        } else {
            s.setClasesRestantes(s.getClasesRestantes() + clases);
            cambiarEstado(s, EstadoSuscripcion.ACTIVA);
            repositorio.actualizar(s);
        }
        return s;
    }

    /** Suscripcion del estudiante en cualquier estado, o null si no tiene. */
    public Suscripcion obtenerSuscripcion(String estudianteId) {
        return repositorio.buscarPorEstudiante(estudianteId);
    }

    /** Todas las suscripciones existentes (cualquier estado). */
    public List<Suscripcion> listarTodas() {
        return repositorio.listarTodas();
    }

    /** Suscripcion ACTIVA con clases disponibles, o null. */
    public Suscripcion obtenerActiva(String estudianteId) {
        Suscripcion s = repositorio.buscarPorEstudiante(estudianteId);
        return (s != null && s.getEstado() == EstadoSuscripcion.ACTIVA && s.getClasesRestantes() > 0) ? s : null;
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
            cambiarEstado(activa, EstadoSuscripcion.AGOTADA);
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
                .filter(s -> s.getEstado() != EstadoSuscripcion.CANCELADA)
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
        if (s.getEstado() == EstadoSuscripcion.CANCELADA) {
            return ResultadoCancelacion.YA_CANCELADA;
        }
        cambiarEstado(s, EstadoSuscripcion.CANCELADA);
        s.setClasesRestantes(0);
        repositorio.actualizar(s);
        return ResultadoCancelacion.CANCELADA;
    }

    /** Estados a los que puede pasar la suscripcion desde su estado actual. */
    public Set<EstadoSuscripcion> siguientesEstados(EstadoSuscripcion estado) {
        return maquina.siguientes(estado);
    }

    /**
     * Aplica un cambio de estado validandolo contra la maquina de estados (grafo).
     * Un cambio no permitido es un error de programacion, no una entrada de usuario.
     */
    private void cambiarEstado(Suscripcion s, EstadoSuscripcion nuevo) {
        EstadoSuscripcion actual = s.getEstado();
        if (!maquina.puedeTransicionar(actual, nuevo)) {
            throw new IllegalStateException(
                    "Transicion de estado no permitida: " + actual + " -> " + nuevo);
        }
        s.setEstado(nuevo);
    }
}
