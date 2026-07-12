package com.yaku.ui;

import com.yaku.model.Estudiante;
import com.yaku.model.Suscripcion;
import com.yaku.model.RegistroAsistencia;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.SuscripcionService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Modulo de asistencias (rediseno Claude Design).
 *
 * Permite registrar la asistencia de un alumno con suscripcion activa (consume
 * una clase de su saldo) y lista las asistencias del dia. Reutiliza
 * {@link TablaGrid} para la tabla "Registradas hoy".
 *
 * Nota: {@code RegistroAsistencia} no almacena el saldo historico, por lo que la
 * columna "saldo tras registro" muestra el saldo actual del alumno (coincide con
 * el caso habitual de una asistencia por dia).
 */
public class AsistenciasVista extends VBox {

    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EstudianteService estudiantes;
    private final SuscripcionService suscripciones;
    private final AsistenciaService asistencias;

    private final ComboBox<Estudiante> comboAlumno = new ComboBox<>();
    private final TablaGrid tabla = new TablaGrid(List.of(
            new TablaGrid.Columna("ALUMNO", 0, false),
            new TablaGrid.Columna("ID", 130, false),
            new TablaGrid.Columna("HORA", 120, false),
            new TablaGrid.Columna("SALDO TRAS REGISTRO", 180, true)),
            "Aún no hay asistencias registradas hoy.")
            .conCabecera("Registradas hoy");

    public AsistenciasVista(EstudianteService estudiantes,
                            SuscripcionService suscripciones,
                            AsistenciaService asistencias) {
        this.estudiantes = estudiantes;
        this.suscripciones = suscripciones;
        this.asistencias = asistencias;

        setSpacing(0);
        VBox encabezado = construirEncabezado();
        Region panel = construirPanelRegistro();
        getChildren().addAll(encabezado, panel, tabla);
        VBox.setMargin(encabezado, new Insets(0, 0, 20, 0));
        VBox.setMargin(panel, new Insets(0, 0, 18, 0));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        refrescar();
    }

    private VBox construirEncabezado() {
        Label titulo = new Label("Asistencias");
        titulo.getStyleClass().add("h1");
        Label sub = new Label("Registro diario · consume una clase del saldo del alumno");
        sub.getStyleClass().add("subtitle");
        return new VBox(5, titulo, sub);
    }

    // ---- Panel "Registrar asistencia" --------------------------------------

    private Region construirPanelRegistro() {
        Label etiqueta = new Label("REGISTRAR ASISTENCIA");
        etiqueta.getStyleClass().add("section-label");

        comboAlumno.setPromptText("Selecciona un alumno con suscripción activa...");
        comboAlumno.setConverter(new StringConverter<>() {
            @Override public String toString(Estudiante e) {
                if (e == null) {
                    return "";
                }
                Suscripcion s = suscripciones.obtenerActiva(e.getId());
                int n = s == null ? 0 : s.getClasesRestantes();
                return e.getNombre() + " " + e.getApellido() + " (" + e.getId() + ") · " + n + " clases";
            }
            @Override public Estudiante fromString(String s) { return null; }
        });
        comboAlumno.setMinWidth(280);
        comboAlumno.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboAlumno, Priority.ALWAYS);

        Button registrar = new Button("Registrar");
        registrar.getStyleClass().add("btn-accent");
        registrar.setOnAction(e -> registrarAsistencia());

        HBox fila = new HBox(12, comboAlumno, registrar);
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(12, etiqueta, fila);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // ---- Datos -------------------------------------------------------------

    private void refrescar() {
        // Alumnos con suscripcion activa (para el selector).
        List<Estudiante> activos = new ArrayList<>();
        for (Estudiante e : estudiantes.listarTodos()) {
            if (suscripciones.obtenerActiva(e.getId()) != null) {
                activos.add(e);
            }
        }
        comboAlumno.setItems(FXCollections.observableArrayList(activos));

        // Asistencias del dia, ordenadas por hora.
        List<RegistroAsistencia> delDia = new ArrayList<>(asistencias.listarDelDia());
        delDia.sort(Comparator.comparing(RegistroAsistencia::getHora));

        List<List<Node>> filas = new ArrayList<>();
        for (RegistroAsistencia a : delDia) {
            filas.add(construirFila(a));
        }
        tabla.mostrar(filas);
        tabla.setContador(delDia.size() + " en total");
    }

    private List<Node> construirFila(RegistroAsistencia a) {
        Estudiante e = estudiantes.buscarPorId(a.getEstudianteId());
        String nombreTxt = e != null ? e.getNombre() + " " + e.getApellido() : a.getEstudianteId();

        Label nombre = new Label(nombreTxt);
        nombre.getStyleClass().add("grid-cell");
        nombre.setStyle("-fx-font-weight:500;");

        Label id = TablaGrid.celda(a.getEstudianteId(), "cell-id");
        id.setStyle("-fx-font-size:13px;");

        Label hora = TablaGrid.celda(a.getHora().format(FMT_HORA), "cell-mono");
        hora.setStyle("-fx-font-size:13px; -fx-text-fill:#5a6b70;");

        Suscripcion s = suscripciones.obtenerSuscripcion(a.getEstudianteId());
        String saldoTxt = s == null ? "—" : s.getClasesRestantes() + " clases";
        Label saldo = TablaGrid.celda(saldoTxt, null);
        saldo.setStyle("-fx-font-size:13px; -fx-text-fill:#5a6b70;");

        return List.of(nombre, id, hora, saldo);
    }

    // ---- Accion ------------------------------------------------------------

    private void registrarAsistencia() {
        Estudiante sel = comboAlumno.getValue();
        if (sel == null) {
            error("Falta el alumno", "Selecciona un alumno con suscripción activa.");
            return;
        }
        AsistenciaService.Registro r = asistencias.registrar(sel.getId());
        switch (r.resultado()) {
            case SIN_SUSCRIPCION_ACTIVA ->
                    error("Sin suscripción activa", "El alumno no tiene una suscripción activa.");
            case SIN_CLASES ->
                    error("Sin clases", "El alumno no tiene clases disponibles en su saldo.");
            case REGISTRADA -> {
                comboAlumno.getSelectionModel().clearSelection();
                comboAlumno.setValue(null);
                refrescar();
            }
        }
    }

    private void error(String encabezado, String detalle) {
        Alert a = new Alert(Alert.AlertType.ERROR, detalle, ButtonType.OK);
        a.setTitle("Error");
        a.setHeaderText(encabezado);
        a.showAndWait();
    }
}
