package com.yaku.ui;

import com.yaku.repository.RepositorioException;
import com.yaku.service.Comando;
import com.yaku.service.GestorDeshacer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Modulo "Historial": pone en evidencia la Pila (LIFO) de acciones (rediseno
 * Claude Design). Cada accion es una tarjeta; la cima (proxima a deshacer) se
 * resalta con un borde verde y una etiqueta. El boton opera sobre la CIMA.
 *
 * Alcance: solo acciones de estudiante (registrar/editar/eliminar). Abonos,
 * asistencias y cancelaciones NO son deshacibles.
 */
public class HistorialVista extends VBox {

    private static final Locale ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_DIA = DateTimeFormatter.ofPattern("d MMM", ES_PE);

    private final GestorDeshacer gestor;
    private final VBox listaCards = new VBox(9);
    private final Button deshacer = new Button("↶ Deshacer la última acción");

    public HistorialVista(GestorDeshacer gestor) {
        this.gestor = gestor;

        setSpacing(0);
        HBox encabezado = construirEncabezado();
        listaCards.setMaxWidth(640);
        getChildren().addAll(encabezado, listaCards);
        VBox.setMargin(encabezado, new Insets(0, 0, 20, 0));

        refrescar();
    }

    private HBox construirEncabezado() {
        Label titulo = new Label("Historial");
        titulo.getStyleClass().add("h1");
        Label sub = new Label("Pila de acciones (LIFO) · la cima es lo próximo a deshacer");
        sub.getStyleClass().add("subtitle");
        VBox textos = new VBox(5, titulo, sub);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        deshacer.getStyleClass().add("btn-primary");
        deshacer.setOnAction(e -> deshacerCima());

        HBox fila = new HBox(16, textos, esp, deshacer);
        fila.setAlignment(Pos.BOTTOM_LEFT);
        return fila;
    }

    private void refrescar() {
        List<Comando> acciones = gestor.acciones();   // cima primero
        listaCards.getChildren().clear();

        if (acciones.isEmpty()) {
            Label vacio = new Label("La pila está vacía: no hay acciones para deshacer.");
            vacio.getStyleClass().add("hist-empty");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vacio.setAlignment(Pos.CENTER);
            listaCards.getChildren().add(vacio);
        } else {
            for (int i = 0; i < acciones.size(); i++) {
                listaCards.getChildren().add(construirCard(acciones.get(i), i + 1, i == 0));
            }
        }
        deshacer.setDisable(!gestor.hayAcciones());
    }

    private HBox construirCard(Comando c, int numero, boolean esCima) {
        Label num = new Label(String.valueOf(numero));
        num.getStyleClass().add("hist-num");
        if (esCima) {
            num.getStyleClass().add("hist-num-top");
        }

        Label desc = new Label(c.descripcion());
        desc.getStyleClass().add("hist-desc");
        Label meta = new Label(metaTexto(c));
        meta.getStyleClass().add("hist-meta");
        VBox centro = new VBox(desc, meta);
        HBox.setHgrow(centro, Priority.ALWAYS);
        centro.setMinWidth(0);

        HBox card = new HBox(14, num, centro);
        card.getStyleClass().add("hist-row");
        card.setAlignment(Pos.CENTER_LEFT);
        if (esCima) {
            card.getStyleClass().add("hist-row-top");
            Label badge = new Label("próxima a deshacer");
            badge.getStyleClass().addAll("badge", "badge-activa");
            card.getChildren().add(badge);
        }
        return card;
    }

    /** Texto "hoy · 11:20" / "ayer · 17:04" / "8 jul · 09:50" segun el momento del comando. */
    private String metaTexto(Comando c) {
        LocalDate dia = c.momento().toLocalDate();
        LocalDate hoy = LocalDate.now();
        String etiqueta;
        if (dia.equals(hoy)) {
            etiqueta = "hoy";
        } else if (dia.equals(hoy.minusDays(1))) {
            etiqueta = "ayer";
        } else {
            etiqueta = dia.format(FMT_DIA);
        }
        return etiqueta + " · " + c.momento().format(FMT_HORA);
    }

    private void deshacerCima() {
        if (!gestor.hayAcciones()) {
            return;
        }
        try {
            gestor.deshacer();
        } catch (RepositorioException | IllegalStateException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            a.setHeaderText("No se pudo deshacer");
            a.showAndWait();
        }
        refrescar();
    }
}
