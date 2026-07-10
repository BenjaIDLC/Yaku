package com.yaku.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Arbol binario de busqueda (BST) generico, ordenado por una clave de tipo
 * {@code String} que se extrae de cada valor.
 *
 * Implementacion propia (no usa {@code TreeMap}/{@code TreeSet}) para el curso
 * de Algoritmos y Estructuras de Datos. Ofrece:
 * <ul>
 *   <li>{@link #insertar} / {@link #eliminar} — O(altura).</li>
 *   <li>{@link #enOrden} — recorrido en-orden, devuelve los valores ordenados.</li>
 *   <li>{@link #buscarPorPrefijo} — busqueda por prefijo con poda de ramas.</li>
 * </ul>
 *
 * Se asume que las claves son unicas; si se inserta una clave ya existente se
 * reemplaza el valor asociado sin alterar la estructura.
 */
public class ArbolBinarioBusqueda<T> {

    private final class Nodo {
        String clave;
        T valor;
        Nodo izquierdo;
        Nodo derecho;

        Nodo(String clave, T valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private final Function<T, String> claveDe;
    private Nodo raiz;
    private int tamano;

    /** @param claveDe funcion que obtiene la clave de ordenamiento de cada valor. */
    public ArbolBinarioBusqueda(Function<T, String> claveDe) {
        this.claveDe = claveDe;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    // ---- Insercion -------------------------------------------------------

    public void insertar(T valor) {
        raiz = insertar(raiz, claveDe.apply(valor), valor);
    }

    private Nodo insertar(Nodo n, String clave, T valor) {
        if (n == null) {
            tamano++;
            return new Nodo(clave, valor);
        }
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            n.izquierdo = insertar(n.izquierdo, clave, valor);
        } else if (cmp > 0) {
            n.derecho = insertar(n.derecho, clave, valor);
        } else {
            n.valor = valor; // clave existente: se reemplaza el valor
        }
        return n;
    }

    // ---- Eliminacion -----------------------------------------------------

    /** @return true si existia un nodo con esa clave y fue eliminado. */
    public boolean eliminar(String clave) {
        int antes = tamano;
        raiz = eliminar(raiz, clave);
        return tamano < antes;
    }

    private Nodo eliminar(Nodo n, String clave) {
        if (n == null) {
            return null;
        }
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            n.izquierdo = eliminar(n.izquierdo, clave);
        } else if (cmp > 0) {
            n.derecho = eliminar(n.derecho, clave);
        } else {
            tamano--;
            // Casos 1 y 2: cero o un hijo.
            if (n.izquierdo == null) return n.derecho;
            if (n.derecho == null) return n.izquierdo;
            // Caso 3: dos hijos. Se reemplaza por el sucesor en-orden
            // (el minimo del subarbol derecho) y se elimina ese sucesor.
            Nodo sucesor = minimo(n.derecho);
            n.clave = sucesor.clave;
            n.valor = sucesor.valor;
            n.derecho = eliminarMinimo(n.derecho);
        }
        return n;
    }

    private Nodo minimo(Nodo n) {
        while (n.izquierdo != null) {
            n = n.izquierdo;
        }
        return n;
    }

    /** Elimina el nodo minimo del subarbol sin tocar el contador (ya ajustado). */
    private Nodo eliminarMinimo(Nodo n) {
        if (n.izquierdo == null) {
            return n.derecho;
        }
        n.izquierdo = eliminarMinimo(n.izquierdo);
        return n;
    }

    // ---- Busqueda --------------------------------------------------------

    /** Busqueda exacta por clave. @return el valor, o null si no existe. */
    public T buscar(String clave) {
        Nodo n = raiz;
        while (n != null) {
            int cmp = clave.compareTo(n.clave);
            if (cmp < 0) n = n.izquierdo;
            else if (cmp > 0) n = n.derecho;
            else return n.valor;
        }
        return null;
    }

    /**
     * Devuelve, ordenados, todos los valores cuya clave empieza por {@code prefijo}.
     * Poda las ramas que no pueden contener coincidencias, evitando recorrer todo
     * el arbol.
     */
    public List<T> buscarPorPrefijo(String prefijo) {
        List<T> resultados = new ArrayList<>();
        buscarPorPrefijo(raiz, prefijo, resultados);
        return resultados;
    }

    private void buscarPorPrefijo(Nodo n, String prefijo, List<T> acc) {
        if (n == null) {
            return;
        }
        if (n.clave.startsWith(prefijo)) {
            // Este nodo coincide; ambos subarboles pueden tener coincidencias.
            buscarPorPrefijo(n.izquierdo, prefijo, acc);
            acc.add(n.valor);
            buscarPorPrefijo(n.derecho, prefijo, acc);
        } else if (prefijo.compareTo(n.clave) < 0) {
            // El prefijo es menor: las coincidencias solo pueden estar a la izquierda.
            buscarPorPrefijo(n.izquierdo, prefijo, acc);
        } else {
            // El prefijo es mayor: las coincidencias solo pueden estar a la derecha.
            buscarPorPrefijo(n.derecho, prefijo, acc);
        }
    }

    // ---- Recorrido -------------------------------------------------------

    /** Recorrido en-orden: devuelve todos los valores ordenados por clave. */
    public List<T> enOrden() {
        List<T> acc = new ArrayList<>();
        enOrden(raiz, acc);
        return acc;
    }

    private void enOrden(Nodo n, List<T> acc) {
        if (n == null) {
            return;
        }
        enOrden(n.izquierdo, acc);
        acc.add(n.valor);
        enOrden(n.derecho, acc);
    }
}
