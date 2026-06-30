package com.yaku.service;

import com.yaku.model.RegistroAsistencia;
import com.yaku.model.Suscripcion;
import com.yaku.repository.IAsistenciaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Reglas de negocio de las asistencias.
 *
 * Decide cuando se puede registrar una asistencia (la suscripcion debe estar
 * ACTIVA y con clases disponibles), persiste el registro y consume una clase
 * delegando esa parte en {@link SuscripcionService}. No sabe si los datos van a
 * MySQL o a memoria, y nunca imprime: devuelve resultados para que lo haga la vista.
 */
public class AsistenciaService {

    /** Desenlaces posibles de un registro, sin texto: el texto lo pone la vista. */
    public enum Resultado { REGISTRADA, SIN_SUSCRIPCION_ACTIVA, SIN_CLASES }

    /** Resultado de registrar: que paso y, si se registro, el detalle. */
    public record Registro(Resultado resultado, RegistroAsistencia asistencia,
                           int clasesRestantes, boolean agotada) {}

    private final IAsistenciaRepository repositorio;
    private final SuscripcionService suscripcionService;

    public AsistenciaService(IAsistenciaRepository repositorio, SuscripcionService suscripcionService) {
        this.repositorio = repositorio;
        this.suscripcionService = suscripcionService;
    }

    public Registro registrar(String estudianteId) {
        Suscripcion activa = suscripcionService.obtenerActiva(estudianteId);
        if (activa == null) {
            return new Registro(Resultado.SIN_SUSCRIPCION_ACTIVA, null, 0, false);
        }
        if (activa.getClasesRestantes() <= 0) {
            return new Registro(Resultado.SIN_CLASES, null, 0, false);
        }

        RegistroAsistencia asistencia = new RegistroAsistencia(
                0, estudianteId, LocalDate.now(), LocalTime.now().withNano(0));
        repositorio.guardar(asistencia);

        // Solo tras guardar con exito se consume la clase.
        boolean agotada = suscripcionService.registrarUsoClase(activa);
        return new Registro(Resultado.REGISTRADA, asistencia, activa.getClasesRestantes(), agotada);
    }

    public List<RegistroAsistencia> listarDelDia() {
        return repositorio.listarPorFecha(LocalDate.now());
    }
}
