package com.yaku.service;

import com.yaku.estructuras.ArbolBinarioBusqueda;
import com.yaku.model.Estudiante;
import com.yaku.repository.IEstudianteRepository;

import java.text.Normalizer;
import java.util.List;

/**
 * Reglas de negocio de los estudiantes.
 *
 * Aqui vive la unica regla del dominio: la generacion del id correlativo
 * ("EST001", "EST002", ...). No sabe si los datos van a MySQL o a memoria
 * —depende solo de {@link IEstudianteRepository}— y nunca imprime: devuelve
 * datos para que la vista decida que mostrar.
 *
 * Mantiene ademas un indice en memoria (arbol binario de busqueda) ordenado
 * por nombre, que se sincroniza en cada alta/edicion/baja. El listado ordenado
 * y la busqueda por nombre se resuelven con ese arbol.
 */
public class EstudianteService {

    /** Desenlaces posibles de una eliminacion; el texto lo pone la vista. */
    public enum ResultadoEliminacion { ELIMINADO, NO_EXISTE, TIENE_SUSCRIPCION_ACTIVA }

    private final IEstudianteRepository repositorio;
    private final SuscripcionService suscripcionService;

    /** Indice ordenado por "apellido nombre id" para busqueda y listado. */
    private final ArbolBinarioBusqueda<Estudiante> indicePorNombre =
            new ArbolBinarioBusqueda<>(EstudianteService::claveNombre);

    public EstudianteService(IEstudianteRepository repositorio, SuscripcionService suscripcionService) {
        this.repositorio = repositorio;
        this.suscripcionService = suscripcionService;
        // Se carga el indice con lo que ya exista en el repositorio.
        for (Estudiante e : repositorio.listarTodos()) {
            indicePorNombre.insertar(e);
        }
    }

    /** Registra un estudiante con id autogenerado y lo devuelve ya persistido. */
    public Estudiante registrar(String nombre, String apellido, String telefono) {
        Estudiante nuevo = new Estudiante(generarNuevoId(), nombre, apellido, telefono);
        repositorio.guardar(nuevo);
        indicePorNombre.insertar(nuevo);
        return nuevo;
    }

    /** Actualiza los datos de un estudiante. Devuelve el estudiante actualizado, o null si no existe. */
    public Estudiante editar(String id, String nombre, String apellido, String telefono) {
        Estudiante actual = repositorio.buscarPorId(id);
        if (actual == null) {
            return null;
        }
        // Se quita del indice con la clave vieja antes de mutar el estudiante.
        indicePorNombre.eliminar(claveNombre(actual));
        actual.setNombre(nombre);
        actual.setApellido(apellido);
        actual.setTelefono(telefono);
        repositorio.actualizar(actual);
        // Se reinserta con la clave nueva.
        indicePorNombre.insertar(actual);
        return actual;
    }

    /** Elimina un estudiante, salvo que tenga una suscripcion activa con clases disponibles. */
    public ResultadoEliminacion eliminar(String id) {
        Estudiante actual = repositorio.buscarPorId(id);
        if (actual == null) {
            return ResultadoEliminacion.NO_EXISTE;
        }
        if (suscripcionService.obtenerActiva(id) != null) {
            return ResultadoEliminacion.TIENE_SUSCRIPCION_ACTIVA;
        }
        repositorio.eliminar(id);
        indicePorNombre.eliminar(claveNombre(actual));
        return ResultadoEliminacion.ELIMINADO;
    }

    /** Devuelve el estudiante con ese id, o null si no existe. */
    public Estudiante buscarPorId(String id) {
        return repositorio.buscarPorId(id);
    }

    /**
     * Busca estudiantes cuyo "apellido nombre" empiece por el texto dado,
     * usando el arbol binario de busqueda. Devuelve los resultados ya ordenados.
     */
    public List<Estudiante> buscarPorNombre(String texto) {
        return indicePorNombre.buscarPorPrefijo(normalizar(texto));
    }

    /** Devuelve todos los estudiantes ordenados (recorrido en-orden del arbol). */
    public List<Estudiante> listarTodos() {
        return indicePorNombre.enOrden();
    }

    /** Clave de ordenamiento: "apellido nombre id", normalizada y unica por id. */
    private static String claveNombre(Estudiante e) {
        return normalizar(e.getApellido()) + " " + normalizar(e.getNombre()) + " " + e.getId();
    }

    /** Pasa a minusculas y quita tildes para que el orden/busqueda ignore acentos. */
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }

    private String generarNuevoId() {
        String ultimo = repositorio.obtenerUltimoId(); // ej: "EST007", o null si no hay
        if (ultimo == null) {
            return "EST001";
        }
        int numero = Integer.parseInt(ultimo.substring(3)) + 1;
        return String.format("EST%03d", numero);
    }
}
