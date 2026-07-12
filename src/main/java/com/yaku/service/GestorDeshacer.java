package com.yaku.service;

import com.yaku.estructuras.Pila;

import java.util.List;

/**
 * Historial de acciones reversibles, respaldado por una {@link Pila} de
 * {@link Comando}. La ultima accion registrada es la primera en poder
 * deshacerse (LIFO).
 *
 * La vista registra un comando tras cada accion exitosa y, cuando el usuario
 * pide deshacer, saca el de la cima y lo ejecuta.
 */
public class GestorDeshacer {

    private final Pila<Comando> historial = new Pila<>();

    /** Apila una accion ya realizada junto con su forma de revertirse. */
    public void registrar(Comando comando) {
        historial.apilar(comando);
    }

    /** @return true si hay al menos una accion que se pueda deshacer. */
    public boolean hayAcciones() {
        return !historial.estaVacia();
    }

    /**
     * Acciones en el historial, de la mas reciente (cima, proxima a deshacerse)
     * a la mas antigua. Solo para mostrar; no modifica la pila.
     */
    public List<Comando> acciones() {
        return historial.aLista();
    }

    /**
     * Descripcion de la accion que se desharia a continuacion, o {@code null}
     * si no hay ninguna. Sirve para el rotulo del menu.
     */
    public String descripcionPendiente() {
        return historial.estaVacia() ? null : historial.cima().descripcion();
    }

    /**
     * Saca la ultima accion, la revierte y la devuelve.
     * @throws IllegalStateException si no hay acciones que deshacer.
     */
    public Comando deshacer() {
        Comando comando = historial.desapilar();
        comando.deshacer();
        return comando;
    }
}
