package com.yaku.estructuras;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Grafo dirigido generico, representado con listas de adyacencia.
 *
 * Implementacion propia para el curso de Algoritmos y Estructuras de Datos.
 * Cada vertice apunta a un conjunto de vertices destino (sus aristas salientes).
 * Ofrece consulta de aristas/adyacentes y un recorrido en anchura (BFS) para
 * calcular los vertices alcanzables desde uno dado.
 */
public class GrafoDirigido<T> {

    /** vertice -> destinos de sus aristas salientes. Orden de insercion preservado. */
    private final Map<T, Set<T>> adyacencia = new LinkedHashMap<>();

    /** Agrega un vertice aislado si aun no existe. */
    public void agregarVertice(T vertice) {
        adyacencia.putIfAbsent(vertice, new LinkedHashSet<>());
    }

    /** Agrega una arista dirigida origen -> destino (crea los vertices si faltan). */
    public void agregarArista(T origen, T destino) {
        agregarVertice(origen);
        agregarVertice(destino);
        adyacencia.get(origen).add(destino);
    }

    /** @return true si existe la arista dirigida origen -> destino. */
    public boolean existeArista(T origen, T destino) {
        Set<T> destinos = adyacencia.get(origen);
        return destinos != null && destinos.contains(destino);
    }

    /** Vertices destino directamente alcanzables desde {@code origen} (solo lectura). */
    public Set<T> adyacentes(T origen) {
        return Collections.unmodifiableSet(
                adyacencia.getOrDefault(origen, Collections.emptySet()));
    }

    /** Todos los vertices del grafo (solo lectura). */
    public Set<T> vertices() {
        return Collections.unmodifiableSet(adyacencia.keySet());
    }

    public int cantidadVertices() {
        return adyacencia.size();
    }

    public int cantidadAristas() {
        int total = 0;
        for (Set<T> destinos : adyacencia.values()) {
            total += destinos.size();
        }
        return total;
    }

    /**
     * Recorrido en anchura (BFS) desde {@code origen}. Devuelve los vertices
     * alcanzables (sin incluir el propio origen), en el orden en que se visitan.
     */
    public List<T> alcanzablesDesde(T origen) {
        List<T> visitados = new ArrayList<>();
        if (!adyacencia.containsKey(origen)) {
            return visitados;
        }
        Set<T> marcados = new LinkedHashSet<>();
        Queue<T> cola = new ArrayDeque<>();
        marcados.add(origen);
        cola.add(origen);
        while (!cola.isEmpty()) {
            T actual = cola.poll();
            for (T vecino : adyacencia.get(actual)) {
                if (marcados.add(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return visitados;
    }
}
