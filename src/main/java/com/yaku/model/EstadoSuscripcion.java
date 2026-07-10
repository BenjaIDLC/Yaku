package com.yaku.model;

/**
 * Estados posibles de una suscripcion.
 *
 * <ul>
 *   <li>{@code ACTIVA}    — tiene clases disponibles.</li>
 *   <li>{@code AGOTADA}   — sin clases (saldo en 0), a la espera de recarga.</li>
 *   <li>{@code CANCELADA} — anulada; su saldo queda en 0.</li>
 * </ul>
 *
 * Las transiciones permitidas entre estos estados las define
 * {@code MaquinaEstadosSuscripcion} sobre un grafo dirigido.
 */
public enum EstadoSuscripcion {
    ACTIVA,
    AGOTADA,
    CANCELADA
}
