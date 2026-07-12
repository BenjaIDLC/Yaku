package com.yaku.ui;

import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Estudiante;
import com.yaku.model.Paquete;
import com.yaku.model.Suscripcion;
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
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Modulo de suscripciones (rediseno Claude Design).
 *
 * Muestra el catalogo de paquetes de clases y la tabla de suscripciones con sus
 * estados y saldos. Reutiliza {@link TablaGrid} para la tabla. Las acciones son
 * abonar clases (crea o recarga la suscripcion) y cancelar. Segun el modelo del
 * producto, abonar/cancelar NO son deshacibles.
 */
public class SuscripcionesVista extends VBox {

    private static final Locale ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("d MMM yyyy", ES_PE);

    private final EstudianteService estudiantes;
    private final SuscripcionService suscripciones;

    private final TablaGrid tabla = new TablaGrid(List.of(
            new TablaGrid.Columna("ALUMNO", 0, false),
            new TablaGrid.Columna("ESTADO", 140, false),
            new TablaGrid.Columna("CLASES", 120, true),
            new TablaGrid.Columna("INICIO", 130, false),
            new TablaGrid.Columna("ACCIONES", 210, true)),
            "Aún no hay suscripciones. Usa \"Abonar clases\" para crear una.");

    public SuscripcionesVista(EstudianteService estudiantes, SuscripcionService suscripciones) {
        this.estudiantes = estudiantes;
        this.suscripciones = suscripciones;

        setSpacing(0);
        HBox encabezado = construirEncabezado();
        Region catalogo = construirCatalogo();
        getChildren().addAll(encabezado, catalogo, tabla);
        VBox.setMargin(encabezado, new Insets(0, 0, 20, 0));
        VBox.setMargin(catalogo, new Insets(0, 0, 18, 0));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        refrescar();
    }

    // ---- Encabezado --------------------------------------------------------

    private HBox construirEncabezado() {
        Label titulo = new Label("Suscripciones");
        titulo.getStyleClass().add("h1");
        Label sub = new Label("Paquetes de clases prepagadas y estados");
        sub.getStyleClass().add("subtitle");
        VBox textos = new VBox(5, titulo, sub);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Button abonar = new Button("Abonar clases");
        abonar.getStyleClass().add("btn-primary");
        abonar.setOnAction(e -> abonarClases(null));

        HBox fila = new HBox(16, textos, esp, abonar);
        fila.setAlignment(Pos.BOTTOM_LEFT);
        return fila;
    }

    // ---- Catalogo de paquetes ---------------------------------------------

    private Region construirCatalogo() {
        HBox fila = new HBox(14);
        List<Paquete> estandar = Paquete.catalogo();
        for (Paquete p : estandar) {
            fila.getChildren().add(tarjetaPaquete(
                    p.nombre(),
                    p.clases() + " clases",
                    String.format("S/ %.0f", p.precio()),
                    String.format(ES_PE, "S/ %.2f / clase", p.precio() / p.clases())));
        }
        fila.getChildren().add(tarjetaPaquete(
                "Personalizado",
                "Elige la cantidad",
                String.format("S/ %.2f", Paquete.PRECIO_POR_CLASE),
                "por clase"));
        for (Node card : fila.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
            ((Region) card).setMaxWidth(Double.MAX_VALUE);
        }
        return fila;
    }

    private Region tarjetaPaquete(String nombre, String detalle, String precio, String porClase) {
        Label lNombre = new Label(nombre);
        lNombre.getStyleClass().add("pkg-nombre");
        Label lDetalle = new Label(detalle);
        lDetalle.getStyleClass().add("pkg-detalle");
        VBox izq = new VBox(2, lNombre, lDetalle);

        Label lPrecio = new Label(precio);
        lPrecio.getStyleClass().add("pkg-precio");
        Label lPorClase = new Label(porClase);
        lPorClase.getStyleClass().add("pkg-porclase");
        VBox der = new VBox(0, lPrecio, lPorClase);
        der.setAlignment(Pos.CENTER_RIGHT);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        HBox card = new HBox(izq, esp, der);
        card.getStyleClass().add("pkg-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    // ---- Tabla -------------------------------------------------------------

    private void refrescar() {
        List<Fila> filas = new ArrayList<>();
        for (Suscripcion s : suscripciones.listarTodas()) {
            Estudiante e = estudiantes.buscarPorId(s.getEstudianteId());
            if (e != null) {
                filas.add(new Fila(e, s));
            }
        }
        filas.sort(Comparator
                .comparing((Fila f) -> f.estudiante.getApellido(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(f -> f.estudiante.getNombre(), String.CASE_INSENSITIVE_ORDER));

        List<List<Node>> celdas = new ArrayList<>();
        for (Fila f : filas) {
            celdas.add(construirFila(f.estudiante, f.suscripcion));
        }
        tabla.mostrar(celdas);
    }

    private List<Node> construirFila(Estudiante e, Suscripcion s) {
        // Alumno: nombre + id (dos lineas)
        Label nombre = new Label(e.getNombre() + " " + e.getApellido());
        nombre.getStyleClass().add("grid-cell");
        nombre.setStyle("-fx-font-weight:500;");
        Label id = new Label(e.getId());
        id.getStyleClass().add("cell-id");
        id.setStyle("-fx-font-size:12px;");
        VBox alumno = new VBox(1, nombre, id);

        Label clases = TablaGrid.celda(String.valueOf(s.getClasesRestantes()), "cell-mono");
        if (s.getEstado() != EstadoSuscripcion.CANCELADA
                && s.getClasesRestantes() <= SuscripcionService.UMBRAL_SALDO_BAJO) {
            clases.setStyle("-fx-text-fill:#a8722a;");   // saldo bajo: ambar
        }

        Label inicio = TablaGrid.celda(s.getFechaInicio().format(FMT_FECHA), "cell-mono");
        inicio.setStyle("-fx-font-size:12.5px; -fx-text-fill:#5a6b70;");

        Button abonar = TablaGrid.botonPill("Abonar", true);
        abonar.setOnAction(ev -> abonarClases(e));
        Button cancelar = TablaGrid.botonPill("Cancelar", false);
        cancelar.setDisable(s.getEstado() == EstadoSuscripcion.CANCELADA);
        cancelar.setOnAction(ev -> cancelarSuscripcion(e));
        HBox acciones = new HBox(7, abonar, cancelar);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        return List.of(alumno, TablaGrid.badge(s), clases, inicio, acciones);
    }

    /** Par (estudiante, suscripcion) para ordenar antes de pintar. */
    private record Fila(Estudiante estudiante, Suscripcion suscripcion) {}

    // ---- Acciones ----------------------------------------------------------

    /** Abre el dialogo de abono y aplica el abono. Si {@code fijo} no es null, el alumno viene dado. */
    private void abonarClases(Estudiante fijo) {
        List<Estudiante> alumnos = estudiantes.listarTodos();
        if (fijo == null && alumnos.isEmpty()) {
            error("Sin alumnos", "Primero registra un alumno en la pestaña Estudiantes.");
            return;
        }

        ComboBox<Estudiante> comboAlumno = new ComboBox<>(FXCollections.observableArrayList(alumnos));
        comboAlumno.setConverter(new StringConverter<>() {
            @Override public String toString(Estudiante e) {
                return e == null ? "" : e.getNombre() + " " + e.getApellido() + " (" + e.getId() + ")";
            }
            @Override public Estudiante fromString(String s) { return null; }
        });
        comboAlumno.setMaxWidth(Double.MAX_VALUE);
        if (fijo != null) {
            comboAlumno.getSelectionModel().select(fijo);
            comboAlumno.setDisable(true);
        }

        List<Paquete> estandar = Paquete.catalogo();
        ComboBox<String> comboPaquete = new ComboBox<>();
        for (Paquete p : estandar) {
            comboPaquete.getItems().add(
                    p.nombre() + " — " + p.clases() + " clases (S/ " + String.format("%.0f", p.precio()) + ")");
        }
        comboPaquete.getItems().add("Personalizado…");
        comboPaquete.getSelectionModel().selectFirst();
        comboPaquete.setMaxWidth(Double.MAX_VALUE);

        TextField campoClases = new TextField();
        campoClases.setPromptText("N.º de clases");
        campoClases.setDisable(true);
        comboPaquete.valueProperty().addListener((o, ov, nv) ->
                campoClases.setDisable(comboPaquete.getSelectionModel().getSelectedIndex() != estandar.size()));

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(16));
        grilla.addRow(0, new Label("Alumno:"), comboAlumno);
        grilla.addRow(1, new Label("Paquete:"), comboPaquete);
        grilla.addRow(2, new Label("Clases:"), campoClases);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Abonar clases");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.getDialogPane().setContent(grilla);

        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        Estudiante alumno = comboAlumno.getValue();
        if (alumno == null) {
            error("Falta el alumno", "Selecciona un alumno.");
            return;
        }

        int idx = comboPaquete.getSelectionModel().getSelectedIndex();
        int clases;
        if (idx >= 0 && idx < estandar.size()) {
            clases = estandar.get(idx).clases();
        } else {
            try {
                clases = Integer.parseInt(campoClases.getText().trim());
            } catch (NumberFormatException ex) {
                error("Cantidad inválida", "Ingresa un número de clases válido.");
                return;
            }
            if (clases <= 0) {
                error("Cantidad inválida", "El número de clases debe ser mayor que 0.");
                return;
            }
        }
        suscripciones.abonar(alumno.getId(), clases);
        refrescar();
    }

    private void cancelarSuscripcion(Estudiante e) {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cancelar la suscripción de " + e.getNombre() + " " + e.getApellido()
                        + "? Se anulará su saldo de clases.",
                ButtonType.YES, ButtonType.NO);
        confirmar.setHeaderText(null);
        confirmar.setTitle("Confirmar cancelación");
        if (confirmar.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        switch (suscripciones.cancelar(e.getId())) {
            case NO_EXISTE -> error("Sin suscripción", "El alumno no tiene una suscripción.");
            case YA_CANCELADA -> error("Ya cancelada", "La suscripción ya estaba cancelada.");
            case CANCELADA -> refrescar();
        }
    }

    private void error(String encabezado, String detalle) {
        Alert a = new Alert(Alert.AlertType.ERROR, detalle, ButtonType.OK);
        a.setTitle("Error");
        a.setHeaderText(encabezado);
        a.showAndWait();
    }
}
