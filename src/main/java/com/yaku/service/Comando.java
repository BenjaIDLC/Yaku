package com.yaku.service;

/**
 * Una accion que ya se ejecuto y que sabe como revertirse a si misma
 * (patron Comando aplicado al "deshacer").
 *
 * Guarda una descripcion legible —para mostrarla en el menu— y la operacion
 * inversa como un {@link Runnable}. El {@link GestorDeshacer} apila estos
 * comandos y, al deshacer, ejecuta {@link #deshacer()} del ultimo apilado.
 */
public final class Comando {

    private final String descripcion;
    private final Runnable accionInversa;

    public Comando(String descripcion, Runnable accionInversa) {
        this.descripcion = descripcion;
        this.accionInversa = accionInversa;
    }

    /** Ejecuta la operacion inversa que revierte esta accion. */
    public void deshacer() {
        accionInversa.run();
    }

    /** Texto legible de la accion, ej: "Registro de EST005". */
    public String descripcion() {
        return descripcion;
    }
}
