package com.yaku;

import com.yaku.db.ConexionDB;
import com.yaku.repository.*;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.GestorDeshacer;
import com.yaku.service.SuscripcionService;
import com.yaku.view.ConsolaView;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            ConexionDB.getInstancia();
        } catch (SQLException e) {
            System.err.println("ERROR: no se pudo conectar a la base de datos (" + e.getMessage() + ").");
            System.err.println("Verifica que la carpeta ./data sea accesible y que no haya otra "
                    + "instancia de Yaku usando la base.");
            return;
        }
        System.out.println("Conexion a la base de datos establecida.");

        IEstudianteRepository estudianteRepo = new EstudianteRepositorySQL();
        ISuscripcionRepository suscripcionRepo = new SuscripcionRepositorySQL();
        IAsistenciaRepository asistenciaRepo = new AsistenciaRepositorySQL();

        SuscripcionService suscripcionService = new SuscripcionService(suscripcionRepo);
        EstudianteService estudianteService = new EstudianteService(estudianteRepo, suscripcionService);
        AsistenciaService asistenciaService = new AsistenciaService(asistenciaRepo, suscripcionService);

        // Historial de "deshacer" (pila de comandos), compartido por la vista.
        GestorDeshacer gestorDeshacer = new GestorDeshacer();

        ConsolaView vista = new ConsolaView(estudianteService, suscripcionService,
                asistenciaService, gestorDeshacer);
        vista.iniciar();
    }
}
