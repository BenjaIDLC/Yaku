package com.yaku.repository;

/**
 * Error de acceso a datos. La capa de repositorio lanza esta excepcion en lugar
 * de imprimir el error: asi quien decide como comunicarlo al usuario es la capa
 * de presentacion (la vista), no el repositorio.
 */
public class RepositorioException extends RuntimeException {
    public RepositorioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
