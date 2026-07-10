package com.yaku.service;

import com.yaku.estructuras.GrafoDirigido;
import com.yaku.model.EstadoSuscripcion;

import java.util.Set;

/**
 * Maquina de estados de una suscripcion, modelada como un grafo dirigido.
 *
 * Los vertices son los {@link EstadoSuscripcion} y las aristas son las
 * transiciones permitidas:
 * <pre>
 *   ACTIVA    -> AGOTADA      (se consumio la ultima clase)
 *   ACTIVA    -> CANCELADA    (cancelacion)
 *   AGOTADA   -> ACTIVA       (recarga de clases)
 *   AGOTADA   -> CANCELADA    (cancelacion)
 *   CANCELADA -> ACTIVA       (reactivacion al abonar clases)
 * </pre>
 * Toda transicion de estado en {@link SuscripcionService} se valida contra este
 * grafo, de modo que un cambio no permitido (p. ej. CANCELADA -> AGOTADA) queda
 * bloqueado en un unico lugar.
 */
public class MaquinaEstadosSuscripcion {

    private final GrafoDirigido<EstadoSuscripcion> transiciones = new GrafoDirigido<>();

    public MaquinaEstadosSuscripcion() {
        for (EstadoSuscripcion estado : EstadoSuscripcion.values()) {
            transiciones.agregarVertice(estado);
        }
        transiciones.agregarArista(EstadoSuscripcion.ACTIVA,    EstadoSuscripcion.AGOTADA);
        transiciones.agregarArista(EstadoSuscripcion.ACTIVA,    EstadoSuscripcion.CANCELADA);
        transiciones.agregarArista(EstadoSuscripcion.AGOTADA,   EstadoSuscripcion.ACTIVA);
        transiciones.agregarArista(EstadoSuscripcion.AGOTADA,   EstadoSuscripcion.CANCELADA);
        transiciones.agregarArista(EstadoSuscripcion.CANCELADA, EstadoSuscripcion.ACTIVA);
    }

    /**
     * @return true si se puede pasar de {@code desde} a {@code hacia}. Mantenerse
     *         en el mismo estado siempre es valido (no es una transicion real).
     */
    public boolean puedeTransicionar(EstadoSuscripcion desde, EstadoSuscripcion hacia) {
        return desde == hacia || transiciones.existeArista(desde, hacia);
    }

    /** Estados a los que se puede pasar directamente desde {@code estado}. */
    public Set<EstadoSuscripcion> siguientes(EstadoSuscripcion estado) {
        return transiciones.adyacentes(estado);
    }
}
