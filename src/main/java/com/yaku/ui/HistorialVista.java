package com.yaku.ui;

import com.yaku.repository.RepositorioException;
import com.yaku.service.Comando;
import com.yaku.service.GestorDeshacer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Modulo "Historial": pone en evidencia la Pila (LIFO) de acciones.
 *
 * Muestra las acciones apiladas de la mas reciente (cima) a la mas antigua. El
 * boton Deshacer opera sobre la CIMA de la pila —fiel a la semantica LIFO—, no
 * sobre una fila arbitraria.
 */
public class HistorialVista extends BorderPane {

    private final GestorDeshacer gestor;
    private final ObservableList<String> filas = FXCollections.observableArrayList();
    private final ListView<String> lista = new ListView<>(filas);
    private final Button deshacer = new Button("↶ Deshacer la ultima accion");
    private final Label estado = new Label();

    public HistorialVista(GestorDeshacer gestor) {
        this.gestor = gestor;
        setPadding(new Insets(16));

        Label titulo = new Label("Pila de acciones (la cima es lo proximo a deshacer)");
        titulo.setStyle("-fx-font-weight:bold;");
        setTop(titulo);
        BorderPane.setMargin(titulo, new Insets(0, 0, 10, 0));

        lista.setPlaceholder(new Label("La pila esta vacia: no hay acciones para deshacer."));
        setCenter(lista);

        deshacer.setStyle("-fx-background-color:#1565c0; -fx-text-fill:white; -fx-font-weight:bold;");
        deshacer.setOnAction(e -> deshacerCima());
        HBox barra = new HBox(12, deshacer, estado);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(12, 0, 0, 0));
        setBottom(barra);

        refrescar();
    }

    private void refrescar() {
        List<Comando> acciones = gestor.acciones(); // cima primero
        filas.clear();
        for (int i = 0; i < acciones.size(); i++) {
            String marca = (i == 0) ? "  ⟵ proxima a deshacer" : "";
            filas.add((i + 1) + ".  " + acciones.get(i).descripcion() + marca);
        }
        deshacer.setDisable(!gestor.hayAcciones());
    }

    private void deshacerCima() {
        if (!gestor.hayAcciones()) {
            return;
        }
        try {
            Comando hecho = gestor.deshacer();
            estado.setText("Deshecho: " + hecho.descripcion());
        } catch (RepositorioException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            a.setHeaderText("No se pudo deshacer");
            a.showAndWait();
        }
        refrescar();
    }
}
