package com.yaku.estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * Pila (LIFO) generica de implementacion propia, respaldada por una lista
 * enlazada simple.
 *
 * Hecha a mano (no usa {@code java.util.Stack}/{@code Deque}) para el curso de
 * Algoritmos y Estructuras de Datos. Todas las operaciones son O(1):
 * <ul>
 *   <li>{@link #apilar} — inserta en la cima.</li>
 *   <li>{@link #desapilar} — saca y devuelve la cima.</li>
 *   <li>{@link #cima} — mira la cima sin sacarla.</li>
 * </ul>
 *
 * En Yaku se usa para el historial de "deshacer": cada accion reversible se
 * apila y la ultima en entrar es la primera en poder deshacerse.
 */
public class Pila<T> {

    private final class Nodo {
        final T valor;
        final Nodo siguiente;

        Nodo(T valor, Nodo siguiente) {
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Nodo cima;
    private int tamano;

    /** Inserta un elemento en la cima de la pila. */
    public void apilar(T valor) {
        cima = new Nodo(valor, cima);
        tamano++;
    }

    /**
     * Saca y devuelve el elemento de la cima.
     * @throws IllegalStateException si la pila esta vacia.
     */
    public T desapilar() {
        if (cima == null) {
            throw new IllegalStateException("La pila esta vacia");
        }
        T valor = cima.valor;
        cima = cima.siguiente;
        tamano--;
        return valor;
    }

    /**
     * Devuelve el elemento de la cima sin sacarlo.
     * @throws IllegalStateException si la pila esta vacia.
     */
    public T cima() {
        if (cima == null) {
            throw new IllegalStateException("La pila esta vacia");
        }
        return cima.valor;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int tamano() {
        return tamano;
    }

    /**
     * Devuelve los elementos de la cima hacia el fondo, sin modificar la pila.
     * Es solo para inspeccion/visualizacion (ej. mostrar el historial); no
     * altera la semantica LIFO.
     */
    public List<T> aLista() {
        List<T> lista = new ArrayList<>();
        Nodo n = cima;
        while (n != null) {
            lista.add(n.valor);
            n = n.siguiente;
        }
        return lista;
    }
}
