package com.yaku.ui;

import com.yaku.model.Estudiante;
import com.yaku.repository.RepositorioException;
import com.yaku.service.Comando;
import com.yaku.service.EstudianteService;
import com.yaku.service.GestorDeshacer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Optional;

/**
 * Modulo de gestion de estudiantes.
 *
 * La tabla se llena con el recorrido en-orden del BST del servicio, por lo que
 * aparece siempre ordenada por apellido/nombre. El buscador usa la busqueda por
 * prefijo del mismo arbol. Registrar/editar/eliminar apilan su accion inversa en
 * el {@link GestorDeshacer} (misma logica que la vista de consola).
 */
public class EstudiantesVista extends BorderPane {

    private final EstudianteService servicio;
    private final GestorDeshacer gestor;

    private final TableView<Estudiante> tabla = new TableView<>();
    private final ObservableList<Estudiante> datos = FXCollections.observableArrayList();
    private final TextField busqueda = new TextField();

    public EstudiantesVista(EstudianteService servicio, GestorDeshacer gestor) {
        this.servicio = servicio;
        this.gestor = gestor;
        setPadding(new Insets(16));

        setTop(construirBarraAcciones());
        setCenter(construirTabla());
        BorderPane.setMargin(getCenter(), new Insets(12, 0, 0, 0));

        refrescar();
    }

    // ---- Barra superior: buscar + registrar --------------------------------

    private HBox construirBarraAcciones() {
        busqueda.setPromptText("Buscar por apellido o nombre...");
        HBox.setHgrow(busqueda, Priority.ALWAYS);
        // Filtrado en vivo usando el BST (prefijo) o el listado in-orden si esta vacio.
        busqueda.textProperty().addListener((obs, viejo, nuevo) -> refrescar());

        Button registrar = new Button("+ Registrar alumno");
        registrar.setStyle("-fx-background-color:#2e7d32; -fx-text-fill:white; -fx-font-weight:bold;");
        registrar.setOnAction(e -> registrarEstudiante());

        HBox barra = new HBox(10, busqueda, registrar);
        barra.setAlignment(Pos.CENTER_LEFT);
        return barra;
    }

    // ---- Tabla -------------------------------------------------------------

    private TableView<Estudiante> construirTabla() {
        TableColumn<Estudiante, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(90);

        TableColumn<Estudiante, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(160);

        TableColumn<Estudiante, String> colApellido = new TableColumn<>("Apellido");
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colApellido.setPrefWidth(160);

        TableColumn<Estudiante, String> colTelefono = new TableColumn<>("Telefono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(140);

        TableColumn<Estudiante, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(140);
        colAcciones.setCellFactory(col -> new CeldaAcciones());

        tabla.getColumns().add(colId);
        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colApellido);
        tabla.getColumns().add(colTelefono);
        tabla.getColumns().add(colAcciones);
        tabla.setItems(datos);
        tabla.setPlaceholder(new Label("No hay estudiantes para mostrar."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tabla;
    }

    /** Celda con los iconos de editar y eliminar para cada fila. */
    private final class CeldaAcciones extends TableCell<Estudiante, Void> {
        private final Button editar = new Button("✎");   // lapiz
        private final Button eliminar = new Button("✕"); // cruz
        private final HBox caja = new HBox(6, editar, eliminar);

        CeldaAcciones() {
            caja.setAlignment(Pos.CENTER);
            editar.setStyle("-fx-background-color:transparent; -fx-font-size:14;");
            eliminar.setStyle("-fx-background-color:transparent; -fx-text-fill:#c62828; -fx-font-size:14;");
            editar.setOnAction(e -> editarEstudiante(getTableView().getItems().get(getIndex())));
            eliminar.setOnAction(e -> eliminarEstudiante(getTableView().getItems().get(getIndex())));
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : caja);
        }
    }

    // ---- Operaciones -------------------------------------------------------

    private void refrescar() {
        String texto = busqueda.getText();
        if (texto == null || texto.isBlank()) {
            datos.setAll(servicio.listarTodos());          // recorrido in-orden del BST
        } else {
            datos.setAll(servicio.buscarPorNombre(texto));  // busqueda por prefijo del BST
        }
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
