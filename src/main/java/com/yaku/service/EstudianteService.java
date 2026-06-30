package com.yaku.service;

import com.yaku.model.Estudiante;
import com.yaku.repository.IEstudianteRepository;

import java.util.List;

/**
 * Reglas de negocio de los estudiantes.
 *
 * Aqui vive la unica regla del dominio: la generacion del id correlativo
 * ("EST001", "EST002", ...). No sabe si los datos van a MySQL o a memoria
 * —depende solo de {@link IEstudianteRepository}— y nunca imprime: devuelve
 * datos para que la vista decida que mostrar.
 */
public class EstudianteService {

    /** Desenlaces posibles de una eliminacion; el texto lo pone la vista. */
    public enum ResultadoEliminacion { ELIMINADO, NO_EXISTE, TIENE_SUSCRIPCION_ACTIVA }

    private final IEstudianteRepository repositorio;
    private final SuscripcionService suscripcionService;

    public EstudianteService(IEstudianteRepository repositorio, SuscripcionService suscripcionService) {
        this.repositorio = repositorio;
        this.suscripcionService = suscripcionService;
    }

    /** Registra un estudiante con id autogenerado y lo devuelve ya persistido. */
    public Estudiante registrar(String nombre, String apellido, String telefono) {
        Estudiante nuevo = new Estudiante(generarNuevoId(), nombre, apellido, telefono);
        repositorio.guardar(nuevo);
        return nuevo;
    }

    /** Actualiza los datos de un estudiante. Devuelve el estudiante actualizado, o null si no existe. */
    public Estudiante editar(String id, String nombre, String apellido, String telefono) {
        Estudiante actual = repositorio.buscarPorId(id);
        if (actual == null) {
            return null;
        }
        actual.setNombre(nombre);
        actual.setApellido(apellido);
        actual.setTelefono(telefono);
        repositorio.actualizar(actual);
        return actual;
    }

    /** Elimina un estudiante, salvo que tenga una suscripcion activa con clases disponibles. */
    public ResultadoEliminacion eliminar(String id) {
        if (repositorio.buscarPorId(id) == null) {
            return ResultadoEliminacion.NO_EXISTE;
        }
        if (suscripcionService.obtenerActiva(id) != null) {
            return ResultadoEliminacion.TIENE_SUSCRIPCION_ACTIVA;
        }
        repositorio.eliminar(id);
        return ResultadoEliminacion.ELIMINADO;
    }

    /** Devuelve el estudiante con ese id, o null si no existe. */
    public Estudiante buscarPorId(String id) {
        return repositorio.buscarPorId(id);
    }

    /** Devuelve todos los estudiantes ordenados. */
    public List<Estudiante> listarTodos() {
        return repositorio.listarTodos();
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
