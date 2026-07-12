package com.yaku.ui;

import com.yaku.db.ConexionDB;
import com.yaku.repository.AsistenciaRepositoryMemoria;
import com.yaku.repository.AsistenciaRepositorySQL;
import com.yaku.repository.EstudianteRepositoryMemoria;
import com.yaku.repository.EstudianteRepositorySQL;
import com.yaku.repository.IAsistenciaRepository;
import com.yaku.repository.IEstudianteRepository;
import com.yaku.repository.ISuscripcionRepository;
import com.yaku.repository.SuscripcionRepositoryMemoria;
import com.yaku.repository.SuscripcionRepositorySQL;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.GestorDeshacer;
import com.yaku.service.SuscripcionService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Punto de entrada de la interfaz grafica (JavaFX).
 *
 * Es el composition root de la GUI: elige repositorios (H2 o memoria) igual que
 * {@link com.yaku.Main} y arma los mismos servicios, que reutiliza tal cual —la
 * GUI es solo una capa de presentacion distinta a la consola.
 *
 * Navegacion: un dashboard de modulos que, al hacer clic, muestra la vista del
 * modulo en el centro. La barra superior tiene "Volver" para regresar al inicio.
 */
public class AppFX extends Application {

    private EstudianteService estudianteService;
    private GestorDeshacer gestorDeshacer;

    private final BorderPane raiz = new BorderPane();
    private final Button volver = new Button("← Volver");
    private final Label tituloVista = new Label();

    @Override
    public void start(Stage stage) {
        construirServicios();

        volver.setOnAction(e -> mostrarDashboard());
        HBox barra = new HBox(12, volver, tituloVista);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(12, 16, 12, 16));
        barra.setStyle("-fx-background-color:#263238;");
        volver.setStyle("-fx-background-color:#455a64; -fx-text-fill:white;");
        tituloVista.setStyle("-fx-text-fill:white; -fx-font-size:16; -fx-font-weight:bold;");
        raiz.setTop(barra);

        mostrarDashboard();

        stage.setTitle("Yaku — Sistema de gestion");
        stage.setScene(new Scene(raiz, 900, 600));
        stage.show();
    }

    /** Elige persistencia H2 o memoria (misma logica que Main) y arma los servicios. */
    private void construirServicios() {
        IEstudianteRepository estudianteRepo;
        ISuscripcionRepository suscripcionRepo;
        IAsistenciaRepository asistenciaRepo;
        try {
            ConexionDB.getInstancia();
            estudianteRepo = new EstudianteRepositorySQL();
            suscripcionRepo = new SuscripcionRepositorySQL();
            asistenciaRepo = new AsistenciaRepositorySQL();
        } catch (SQLException e) {
            estudianteRepo = new EstudianteRepositoryMemoria();
            suscripcionRepo = new SuscripcionRepositoryMemoria();
            asistenciaRepo = new AsistenciaRepositoryMemoria();
        }
        SuscripcionService suscripcionService = new SuscripcionService(suscripcionRepo);
        estudianteService = new EstudianteService(estudianteRepo, suscripcionService);
        // Los servicios de suscripcion/asistencia quedan listos para futuros modulos.
        new AsistenciaService(asistenciaRepo, suscripcionService);
        gestorDeshacer = new GestorDeshacer();
    }

    // ---- Navegacion --------------------------------------------------------

    private void mostrarDashboard() {
        volver.setVisible(false);
        tituloVista.setText("Inicio");
        raiz.setCenter(construirDashboard());
    }

    private void navegar(String titulo, Node contenido) {
        volver.setVisible(true);
        tituloVista.setText(titulo);
        raiz.setCenter(contenido);
    }

    // ---- Dashboard ---------------------------------------------------------

    private GridPane construirDashboard() {
        GridPane grilla = new GridPane();
        grilla.setHgap(16);
        grilla.setVgap(16);
        grilla.setPadding(new Insets(20));

        // 6 columnas iguales: fila de arriba = 3 tiles (span 2), abajo = 2 tiles (span 3).
        for (int i = 0; i < 6; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 6);
            cc.setHgrow(Priority.ALWAYS);
            grilla.getColumnConstraints().add(cc);
        }
        RowConstraints filaArriba = new RowConstraints();
        filaArriba.setPercentHeight(45);
        filaArriba.setVgrow(Priority.ALWAYS);
        RowConstraints filaAbajo = new RowConstraints();
        filaAbajo.setPercentHeight(55);
        filaAbajo.setVgrow(Priority.ALWAYS);
        grilla.getRowConstraints().addAll(filaArriba, filaAbajo);

        // Fila 0: los tres modulos operativos.
        grilla.add(tile("Estudiantes", "Alta, busqueda, edicion", "#00695c",
                () -> navegar("Estudiantes", new EstudiantesVista(estudianteService, gestorDeshacer))), 0, 0, 2, 1);
        grilla.add(tile("Suscripciones", "Paquetes de clases y estados", "#5e35b1",
                () -> navegar("Suscripciones", placeholder("Suscripciones"))), 2, 0, 2, 1);
        grilla.add(tile("Asistencias", "Registro diario", "#0277bd",
                () -> navegar("Asistencias", placeholder("Asistencias"))), 4, 0, 2, 1);

        // Fila 1: los dos grandes (50/50).
        grilla.add(tile("Reportes", "Saldo bajo y resumenes", "#ef6c00",
                () -> navegar("Reportes", placeholder("Reportes"))), 0, 1, 3, 1);
        grilla.add(tile("Historial", "Deshacer acciones (Pila)", "#c62828",
                () -> navegar("Historial", new HistorialVista(gestorDeshacer))), 3, 1, 3, 1);

        return grilla;
    }

    /** Crea un tile del dashboard: bloque de color con titulo y subtitulo, clickeable. */
    private Button tile(String titulo, String subtitulo, String color, Runnable accion) {
        Label t = new Label(titulo);
        t.setStyle("-fx-font-size:22; -fx-font-weight:bold; -fx-text-fill:white;");
        Label s = new Label(subtitulo);
        s.setStyle("-fx-font-size:13; -fx-text-fill:rgba(255,255,255,0.85);");
        VBox contenido = new VBox(6, t, s);
        contenido.setAlignment(Pos.CENTER_LEFT);

        Button tile = new Button();
        tile.setGraphic(contenido);
        tile.setAlignment(Pos.BOTTOM_LEFT);
        tile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tile.setPadding(new Insets(18));
        tile.setStyle("-fx-background-color:" + color + "; -fx-background-radius:10; -fx-cursor:hand;");
        tile.setOnAction(e -> accion.run());
        GridPane.setHgrow(tile, Priority.ALWAYS);
        GridPane.setVgrow(tile, Priority.ALWAYS);
        return tile;
    }

    /** Contenido temporal para los modulos aun no implementados. */
    private Node placeholder(String modulo) {
        Label l = new Label(modulo + " — proximamente");
        l.setStyle("-fx-font-size:18; -fx-text-fill:#607d8b;");
        return new StackPane(l);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
