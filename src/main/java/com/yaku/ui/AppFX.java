package com.yaku.ui;

import com.yaku.db.ConexionDB;
import com.yaku.repository.AsistenciaRepositorySQL;
import com.yaku.repository.EstudianteRepositorySQL;
import com.yaku.repository.IAsistenciaRepository;
import com.yaku.repository.IEstudianteRepository;
import com.yaku.repository.ISuscripcionRepository;
import com.yaku.repository.SuscripcionRepositorySQL;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.GestorDeshacer;
import com.yaku.service.SuscripcionService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Punto de entrada de la interfaz grafica (JavaFX).
 *
 * Composition root de la GUI: arma los repositorios H2 igual que
 * {@link com.yaku.Main} y construye los mismos servicios, que reutiliza tal cual.
 *
 * Navegacion (rediseno): barra superior fija con pestanas; el contenido de la
 * pestana activa se muestra en el area central desplazable. El look & feel vive
 * en {@code /theme.css}.
 */
public class AppFX extends Application {

    private EstudianteService estudianteService;
    private SuscripcionService suscripcionService;
    private AsistenciaService asistenciaService;
    private GestorDeshacer gestorDeshacer;

    private final BorderPane raiz = new BorderPane();
    private final ScrollPane areaContenido = new ScrollPane();
    private final Map<String, Button> botonesNav = new LinkedHashMap<>();

    @Override
    public void start(Stage stage) {
        if (!construirServicios()) {
            return; // sin base de datos no se puede continuar; ya se aviso al usuario
        }

        raiz.setTop(construirHeader());
        areaContenido.setFitToWidth(true);
        areaContenido.getStyleClass().add("content-scroll");
        raiz.setCenter(areaContenido);

        seleccionar("Inicio");

        Scene escena = new Scene(raiz, 1200, 760);
        escena.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        stage.setTitle("Yaku — Sistema de gestion");
        stage.setScene(escena);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Conecta a H2 (misma logica que Main) y arma los servicios.
     * @return true si se conecto y los servicios quedaron listos; false si no se
     *         pudo conectar (en ese caso ya se mostro el error y la app se cierra).
     */
    private boolean construirServicios() {
        try {
            ConexionDB.getInstancia();
        } catch (SQLException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "No se pudo conectar a la base de datos:\n" + e.getMessage()
                            + "\n\nVerifica que la carpeta ./data sea accesible y que no haya otra "
                            + "instancia de Yaku abierta.");
            alerta.setHeaderText("Error de base de datos");
            alerta.showAndWait();
            Platform.exit();
            return false;
        }
        IEstudianteRepository estudianteRepo = new EstudianteRepositorySQL();
        ISuscripcionRepository suscripcionRepo = new SuscripcionRepositorySQL();
        IAsistenciaRepository asistenciaRepo = new AsistenciaRepositorySQL();
        suscripcionService = new SuscripcionService(suscripcionRepo);
        estudianteService = new EstudianteService(estudianteRepo, suscripcionService);
        asistenciaService = new AsistenciaService(asistenciaRepo, suscripcionService);
        gestorDeshacer = new GestorDeshacer();
        return true;
    }

    // ---- Barra superior ----------------------------------------------------

    private HBox construirHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label marca = new Label("Yaku");
        marca.getStyleClass().add("brand");

        HBox nav = new HBox(3);
        nav.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        for (String pantalla : new String[]{
                "Inicio", "Estudiantes", "Suscripciones", "Asistencias", "Historial"}) {
            Button b = new Button(pantalla);
            b.getStyleClass().add("nav-button");
            b.setOnAction(e -> seleccionar(pantalla));
            botonesNav.put(pantalla, b);
            nav.getChildren().add(b);
        }

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label fecha = new Label(fechaHoy());
        fecha.getStyleClass().addAll("header-date", "mono");

        Label avatar = new Label("RA");
        avatar.getStyleClass().add("avatar");

        HBox derecha = new HBox(14, fecha, avatar);
        derecha.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        header.getChildren().addAll(marca, nav, espaciador, derecha);
        return header;
    }

    private String fechaHoy() {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", new Locale("es", "PE")));
    }

    // ---- Navegacion --------------------------------------------------------

    /** Activa una pestana: resalta su boton y muestra su contenido. */
    private void seleccionar(String pantalla) {
        botonesNav.forEach((nombre, boton) -> {
            boton.getStyleClass().remove("nav-button-active");
            if (nombre.equals(pantalla)) {
                boton.getStyleClass().add("nav-button-active");
            }
        });
        areaContenido.setContent(envolver(contenidoDe(pantalla)));
    }

    /** Devuelve la vista de cada pantalla (placeholder para las aun no rediseñadas). */
    private Node contenidoDe(String pantalla) {
        return switch (pantalla) {
            case "Inicio" -> new InicioVista(estudianteService, suscripcionService, asistenciaService, this::seleccionar);
            case "Estudiantes" -> new EstudiantesVista(estudianteService, suscripcionService, gestorDeshacer);
            case "Suscripciones" -> new SuscripcionesVista(estudianteService, suscripcionService);
            case "Asistencias" -> new AsistenciasVista(estudianteService, suscripcionService, asistenciaService);
            case "Historial" -> new HistorialVista(gestorDeshacer);
            default -> placeholder(pantalla);
        };
    }

    /**
     * Envuelve el contenido replicando el contenedor del prototipo:
     * caja de ancho maximo 1340px centrada horizontalmente (margin:0 auto) con
     * el padding estandar (28 30 56). El StackPane externo ocupa todo el ancho
     * de la ventana y centra la caja; su alto minimo se ata al viewport para
     * que el contenido llene la ventana (y las vistas puedan estirarse) aunque
     * pueda crecer y desplazarse cuando hay mas contenido.
     */
    private Node envolver(Node contenido) {
        StackPane caja = new StackPane(contenido);
        caja.getStyleClass().add("content-pane");
        caja.setMaxWidth(1340);
        caja.setMaxHeight(Double.MAX_VALUE);

        StackPane exterior = new StackPane(caja);
        exterior.minHeightProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                () -> areaContenido.getViewportBounds().getHeight(),
                areaContenido.viewportBoundsProperty()));
        return exterior;
    }

    private Node placeholder(String pantalla) {
        Label l = new Label(pantalla + " — se reconstruira con el nuevo diseño");
        l.getStyleClass().add("muted");
        l.setStyle("-fx-font-size:16;");
        StackPane sp = new StackPane(l);
        sp.setMinHeight(400);
        return sp;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
