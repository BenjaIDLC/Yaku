package com.yaku;

import com.yaku.db.ConexionDB;
import com.yaku.repository.*;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.SuscripcionService;
import com.yaku.view.ConsolaView;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        IEstudianteRepository estudianteRepo;
        ISuscripcionRepository suscripcionRepo;
        IAsistenciaRepository asistenciaRepo;

        try {
            ConexionDB.getInstancia();
            System.out.println("Conexion a la base de datos establecida.");
            estudianteRepo = new EstudianteRepositorySQL();
            suscripcionRepo = new SuscripcionRepositorySQL();
            asistenciaRepo = new AsistenciaRepositorySQL();
        } catch (SQLException e) {
            System.out.println("Aviso: no se pudo conectar a la base de datos (" + e.getMessage() + ").");
            System.out.println("Iniciando en MODO MEMORIA — los datos no se guardaran al cerrar.\n");
            estudianteRepo = new EstudianteRepositoryMemoria();
            suscripcionRepo = new SuscripcionRepositoryMemoria();
            asistenciaRepo = new AsistenciaRepositoryMemoria();
        }

        // Solo cambian los repositorios entre modos: los servicios (las reglas
        // de negocio) son identicos en ambos casos.
        SuscripcionService suscripcionService = new SuscripcionService(suscripcionRepo);
        EstudianteService estudianteService = new EstudianteService(estudianteRepo, suscripcionService);
        AsistenciaService asistenciaService = new AsistenciaService(asistenciaRepo, suscripcionService);

        ConsolaView vista = new ConsolaView(estudianteService, suscripcionService, asistenciaService);
        vista.iniciar();
    }
}
