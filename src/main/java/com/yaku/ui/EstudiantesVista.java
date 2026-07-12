package com.yaku.ui;

import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Estudiante;
import com.yaku.model.Suscripcion;
import com.yaku.repository.RepositorioException;
import com.yaku.service.Comando;
import com.yaku.service.EstudianteService;
import com.yaku.service.GestorDeshacer;
import com.yaku.service.SuscripcionService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.Optional;

/**
 * Modulo de gestion de estudiantes (rediseno Claude Design).
 *
 * La tabla se construye como un "grid" (header + filas HBox con las mismas
 * anchuras de columna) para reproducir exactamente el {@code display:grid} del
 * prototipo: padding 12x22, gap 12, y header alineado a plomo con las filas.
 *
 * Los datos vienen del recorrido en-orden del BST del servicio (orden por
 * apellido/nombre); el buscador usa la busqueda por prefijo del mismo arbol.
 * Registrar/editar/eliminar apilan su accion inversa en el {@link GestorDeshacer}.
 */
public class EstudiantesVista extends VBox {

    // Anchos de columna del prototipo: 108px 1.3fr 1.3fr 1.2fr 140px 84px 130px.
    // Las tres "fr" son flexibles (crecen); el resto es fijo.
    private static final double W_ID = 108, W_SUS = 140, W_SALDO = 84, W_ACC = 130;

    private final EstudianteService servicio;
    private final SuscripcionService suscripciones;
    private final GestorDeshacer gestor;

    private final TextField busqueda = new TextField();
    private final Label subtitulo = new Label();
    private final VBox cuerpo = new VBox();   // filas de la tabla

    public EstudiantesVista(EstudianteService servicio,
                            SuscripcionService suscripciones,
                            GestorDeshacer gestor) {
        this.servicio = servicio;
        this.suscripciones = suscripciones;
        this.gestor = gestor;

        setSpacing(0);
        HBox encabezado = construirEncabezado();
        HBox buscador = construirBuscador();
        VBox tabla = construirTabla();
        getChildren().addAll(encabezado, buscador, tabla);
        // Margenes exactos del prototipo: titulo 20px, buscador 16px.
        VBox.setMargin(encabezado, new Insets(0, 0, 20, 0));
        VBox.setMargin(buscador, new Insets(0, 0, 16, 0));
        VBox.setVgrow(tabla, Priority.ALWAYS);   // la tabla llena el alto disponible

        refrescar();
    }

    // ---- Encabezado: titulo + subtitulo + boton registrar ------------------

    private HBox construirEncabezado() {
        Label titulo = new Label("Estudiantes");
        titulo.getStyleClass().add("h1");
        subtitulo.getStyleClass().add("subtitle");

        VBox textos = new VBox(5, titulo, subtitulo);

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Button registrar = new Button("+ Registrar alumno");
        registrar.getStyleClass().add("btn-primary");
        registrar.setOnAction(e -> registrarEstudiante());

        HBox fila = new HBox(16, textos, espaciador, registrar);
        fila.setAlignment(Pos.BOTTOM_LEFT);   // align-items:flex-end del prototipo
        return fila;
    }

    // ---- Buscador ----------------------------------------------------------

    private HBox construirBuscador() {
        Label lupa = new Label("⌕");
        lupa.setStyle("-fx-text-fill:#9aa5a8; -fx-font-size:16px;");

        busqueda.setPromptText("Buscar por apellido...");
        HBox.setHgrow(busqueda, Priority.ALWAYS);
        busqueda.textProperty().addListener((obs, viejo, nuevo) -> refrescar());

        HBox caja = new HBox(lupa, busqueda);
        caja.getStyleClass().add("search-box");
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.setMaxWidth(420);
        return caja;
    }

    // ---- Tabla (grid) ------------------------------------------------------

    private VBox construirTabla() {
        VBox tabla = new VBox(construirHead(), cuerpo);
        tabla.getStyleClass().add("grid-table");
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        return tabla;
    }

    private HBox construirHead() {
        HBox head = filaBase("grid-head");
        head.getChildren().addAll(
                fijo(th("ID"), W_ID),
                flex(th("NOMBRE")),
                flex(th("APELLIDO")),
                flex(th("TELÉFONO")),
                fijo(th("SUSCRIPCIÓN"), W_SUS),
                fijo(th("SALDO"), W_SALDO),
                fijo(new Region(), W_ACC));   // columna de acciones sin titulo
        return head;
    }

    private HBox construirFila(Estudiante e) {
        HBox fila = filaBase("grid-row");

        Region idCel = fijo(celda(e.getId(), "cell-id"), W_ID);
        Region nomCel = flex(celda(e.getNombre(), "cell-name"));
        Region apeCel = flex(celda(e.getApellido(), null));
        Region telCel = flex(celda(e.getTelefono(), "cell-mono"));

        Suscripcion s = suscripciones.obtenerSuscripcion(e.getId());

        HBox susCel = new HBox(badge(s));
        susCel.setAlignment(Pos.CENTER_LEFT);
        fijo(susCel, W_SUS);

        Label saldo = celda(s == null ? "—" : String.valueOf(s.getClasesRestantes()), "cell-mono");
        fijo(saldo, W_SALDO);

        HBox accCel = new HBox(6, botonEditar(e), botonEliminar(e));
        accCel.setAlignment(Pos.CENTER_RIGHT);   // justify-content:flex-end del prototipo
        fijo(accCel, W_ACC);

        fila.getChildren().addAll(idCel, nomCel, apeCel, telCel, susCel, saldo, accCel);
        return fila;
    }

    /** HBox base de una fila/header: gap 12 y alineacion vertical centrada. */
    private HBox filaBase(String clase) {
        HBox h = new HBox(12);
        h.getStyleClass().add(clase);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private Label th(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("th");
        return l;
    }

    private Label celda(String texto, String clase) {
        Label l = new Label(texto);
        l.getStyleClass().add("grid-cell");
        if (clase != null) {
            l.getStyleClass().add(clase);
        }
        return l;
    }

    /** Badge de estado de la suscripcion (o "Sin suscripcion" si no tiene). */
    private Label badge(Suscripcion s) {
        Label badge = new Label();
        badge.getStyleClass().add("badge");
        if (s == null) {
            badge.setText("Sin suscripción");
            badge.getStyleClass().add("badge-neutral");
        } else if (s.getEstado() == EstadoSuscripcion.ACTIVA) {
            badge.setText("Activa");
            badge.getStyleClass().add("badge-activa");
        } else if (s.getEstado() == EstadoSuscripcion.AGOTADA) {
            badge.setText("Agotada");
            badge.getStyleClass().add("badge-agotada");
        } else {
            badge.setText("Cancelada");
            badge.getStyleClass().add("badge-cancelada");
        }
        return badge;
    }

    // Iconos (paths de 24px, estilo Material) escalados dentro del boton 32x32.
    private static final String SVG_LAPIZ =
            "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 "
            + "0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private static final String SVG_X =
            "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 "
            + "17.59 19 19 17.59 13.41 12z";

    private Button botonEditar(Estudiante e) {
        Button b = botonIcono(SVG_LAPIZ, false);
        b.setOnAction(ev -> editarEstudiante(e));
        return b;
    }

    private Button botonEliminar(Estudiante e) {
        Button b = botonIcono(SVG_X, true);
        b.setOnAction(ev -> eliminarEstudiante(e));
        return b;
    }

    /** Boton-icono cuadrado con un path SVG monocromo (el color lo pone el CSS). */
    private Button botonIcono(String svg, boolean peligro) {
        SVGPath icono = new SVGPath();
        icono.setContent(svg);
        icono.getStyleClass().add("ic");
        icono.setScaleX(0.62);
        icono.setScaleY(0.62);

        Button b = new Button();
        b.setGraphic(icono);
        b.getStyleClass().add("btn-icon");
        if (peligro) {
            b.getStyleClass().add("btn-icon-danger");
        }
        return b;
    }

    /** Fija el ancho de una columna (min = pref = max). */
    private Region fijo(Region n, double w) {
        n.setMinWidth(w);
        n.setPrefWidth(w);
        n.setMaxWidth(w);
        return n;
    }

    /**
     * Marca una columna como flexible. Clave: se fuerza min=pref=0 para que el
     * ancho NO dependa del texto de la celda; asi el HBox reparte el sobrante en
     * partes iguales y header y filas obtienen exactamente la misma "pista" de
     * columna (quedan alineados aunque los textos midan distinto).
     */
    private Region flex(Region n) {
        n.setMinWidth(0);
        n.setPrefWidth(0);
        n.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(n, Priority.ALWAYS);
        return n;
    }

    // ---- Operaciones -------------------------------------------------------

    private void refrescar() {
        String texto = busqueda.getText();
        List<Estudiante> lista = (texto == null || texto.isBlank())
                ? servicio.listarTodos()          // recorrido in-orden del BST
                : servicio.buscarPorNombre(texto);  // busqueda por prefijo del BST

        cuerpo.getChildren().clear();
        for (Estudiante e : lista) {
            cuerpo.getChildren().add(construirFila(e));
        }
        if (lista.isEmpty()) {
            Label vacio = new Label("No hay estudiantes para mostrar.");
            vacio.getStyleClass().add("muted");
            HBox fila = filaBase("grid-row");
            fila.getChildren().add(vacio);
            cuerpo.getChildren().add(fila);
        }
        // Relleno que dibuja las lineas de las filas aun vacias hasta el fondo
        // (efecto "la tabla se ira llenando"); se adapta solo al alto disponible.
        Region relleno = new Region();
        relleno.getStyleClass().add("grid-filler");
        VBox.setVgrow(relleno, Priority.ALWAYS);
        cuerpo.getChildren().add(relleno);

        int total = servicio.listarTodos().size();
        subtitulo.setText("Alta, búsqueda y edición · " + total + " registrados");
    }

    private void registrarEstudiante() {
        Optional<String[]> datosNuevos = dialogoDatos("Registrar alumno", "", "", "");
        datosNuevos.ifPresent(v -> {
            try {
                Estudiante e = servicio.registrar(v[0], v[1], v[2]);
                gestor.registrar(new Comando(
                        "Registro de " + e.getId(),
                        () -> servicio.descartar(e)));
                refrescar();
            } catch (RepositorioException ex) {
                error("No se pudo registrar el estudiante", ex.getMessage());
            }
        });
    }

    private void editarEstudiante(Estudiante actual) {
        String nombreViejo = actual.getNombre();
        String apellidoViejo = actual.getApellido();
        String telefonoViejo = actual.getTelefono();
        Optional<String[]> datosNuevos =
                dialogoDatos("Editar " + actual.getId(), nombreViejo, apellidoViejo, telefonoViejo);
        datosNuevos.ifPresent(v -> {
            try {
                servicio.editar(actual.getId(), v[0], v[1], v[2]);
                gestor.registrar(new Comando(
                        "Edicion de " + actual.getId(),
                        () -> servicio.editar(actual.getId(), nombreViejo, apellidoViejo, telefonoViejo)));
                refrescar();
            } catch (RepositorioException ex) {
                error("No se pudo editar el estudiante", ex.getMessage());
            }
        });
    }

    private void eliminarEstudiante(Estudiante e) {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + e.getNombre() + " " + e.getApellido() + " (" + e.getId() + ")?",
                ButtonType.YES, ButtonType.NO);
        confirmar.setHeaderText(null);
        confirmar.setTitle("Confirmar eliminacion");
        if (confirmar.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        try {
            switch (servicio.eliminar(e.getId())) {
                case NO_EXISTE -> error("No encontrado", "El estudiante ya no existe.");
                case TIENE_SUSCRIPCION_ACTIVA -> error("No se puede eliminar",
                        "El estudiante tiene una suscripcion ACTIVA. Cancelela primero.");
                case ELIMINADO -> {
                    gestor.registrar(new Comando(
                            "Eliminacion de " + e.getId(),
                            () -> servicio.restaurar(e)));
                    refrescar();
                }
            }
        } catch (RepositorioException ex) {
            error("No se pudo eliminar el estudiante", ex.getMessage());
        }
    }

    // ---- Auxiliares de UI --------------------------------------------------

    /** Dialogo con los tres campos; devuelve {nombre, apellido, telefono} o vacio si se cancela. */
    private Optional<String[]> dialogoDatos(String titulo, String nombre, String apellido, String telefono) {
        Dialog<String[]> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField campoNombre = new TextField(nombre);
        TextField campoApellido = new TextField(apellido);
        TextField campoTelefono = new TextField(telefono);

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(16));
        grilla.addRow(0, new Label("Nombre:"), campoNombre);
        grilla.addRow(1, new Label("Apellido:"), campoApellido);
        grilla.addRow(2, new Label("Telefono:"), campoTelefono);
        dialogo.getDialogPane().setContent(grilla);

        dialogo.setResultConverter(boton -> {
            if (boton == ButtonType.OK) {
                return new String[]{
                        campoNombre.getText().trim(),
                        campoApellido.getText().trim(),
                        campoTelefono.getText().trim()};
            }
            return null;
        });
        return dialogo.showAndWait();
    }

    private void error(String encabezado, String detalle) {
        Alert a = new Alert(Alert.AlertType.ERROR, detalle, ButtonType.OK);
        a.setTitle("Error");
        a.setHeaderText(encabezado);
        a.showAndWait();
    }
}
