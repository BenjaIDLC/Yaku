package com.yaku.ui;

import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Suscripcion;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.List;

/**
 * Tabla reutilizable con el look del prototipo (Claude Design): un "grid" donde
 * la cabecera y cada fila comparten exactamente las mismas pistas de columna, de
 * modo que quedan alineadas por construccion (independiente del texto).
 *
 * Cada pantalla define sus columnas ({@link Columna}) y entrega las filas como
 * listas de nodos-celda; la tabla se encarga del ancho, la alineacion, el padding
 * (12x22, gap 12) y el relleno de filas vacias. Incluye ademas fabricas estaticas
 * para las celdas comunes (texto, badge de estado, botones).
 */
public class TablaGrid extends VBox {

    /** Columna: titulo, ancho (px fijo; {@code <= 0} = flexible) y alineacion. */
    public record Columna(String titulo, double ancho, boolean alDerecha) {}

    // Paths SVG (24px, estilo Material) para los botones-icono.
    public static final String ICONO_EDITAR =
            "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 "
            + "0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    public static final String ICONO_ELIMINAR =
            "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 "
            + "17.59 19 19 17.59 13.41 12z";

    private final List<Columna> columnas;
    private final VBox cuerpo = new VBox();
    private final String textoVacio;
    private final Label contador = new Label();   // texto a la derecha de la barra de titulo

    public TablaGrid(List<Columna> columnas) {
        this(columnas, "No hay datos para mostrar.");
    }

    public TablaGrid(List<Columna> columnas, String textoVacio) {
        this.columnas = columnas;
        this.textoVacio = textoVacio;
        getStyleClass().add("grid-table");
        getChildren().addAll(construirHead(), cuerpo);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
    }

    /**
     * Agrega una barra de titulo sobre la cabecera de columnas (dentro de la
     * misma card): titulo a la izquierda y un contador a la derecha (ver
     * {@link #setContador}). Devuelve {@code this} para encadenar.
     */
    public TablaGrid conCabecera(String titulo) {
        Label t = new Label(titulo);
        t.getStyleClass().add("card-title");
        contador.getStyleClass().add("grid-titlebar-count");
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox barra = new HBox(t, esp, contador);
        barra.getStyleClass().add("grid-titlebar");
        barra.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(0, barra);
        getStyleClass().add("con-cabecera");
        return this;
    }

    /** Texto del contador en la barra de titulo (p.ej. "4 en total"). */
    public void setContador(String texto) {
        contador.setText(texto);
    }

    /** Reemplaza las filas. Cada fila es la lista de celdas en el orden de las columnas. */
    public void mostrar(List<List<Node>> filas) {
        cuerpo.getChildren().clear();
        if (filas.isEmpty()) {
            Label vacio = new Label(textoVacio);
            vacio.getStyleClass().add("muted");
            HBox fila = filaBase("grid-row");
            fila.getChildren().add(vacio);
            cuerpo.getChildren().add(fila);
        } else {
            for (List<Node> celdas : filas) {
                cuerpo.getChildren().add(construirFila(celdas));
            }
        }
        // Relleno que dibuja las lineas de las filas aun vacias hasta el fondo.
        Region relleno = new Region();
        relleno.getStyleClass().add("grid-filler");
        VBox.setVgrow(relleno, Priority.ALWAYS);
        cuerpo.getChildren().add(relleno);
    }

    // ---- Construccion interna ----------------------------------------------

    private HBox construirHead() {
        HBox head = filaBase("grid-head");
        for (Columna c : columnas) {
            Label titulo = new Label(c.titulo());
            titulo.getStyleClass().add("th");
            head.getChildren().add(slot(titulo, c));
        }
        return head;
    }

    private HBox construirFila(List<Node> celdas) {
        HBox fila = filaBase("grid-row");
        for (int i = 0; i < columnas.size(); i++) {
            Node contenido = i < celdas.size() && celdas.get(i) != null ? celdas.get(i) : new Region();
            fila.getChildren().add(slot(contenido, columnas.get(i)));
        }
        return fila;
    }

    /**
     * Envuelve una celda aplicando el ancho y la alineacion de su columna. Las
     * columnas flexibles se fuerzan a min=pref=0 para que el ancho no dependa del
     * texto y todas las filas obtengan la misma pista de columna que la cabecera.
     */
    private Region slot(Node contenido, Columna c) {
        HBox slot = new HBox(contenido);
        slot.setAlignment(c.alDerecha() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        if (c.ancho() > 0) {
            slot.setMinWidth(c.ancho());
            slot.setPrefWidth(c.ancho());
            slot.setMaxWidth(c.ancho());
        } else {
            slot.setMinWidth(0);
            slot.setPrefWidth(0);
            slot.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(slot, Priority.ALWAYS);
        }
        return slot;
    }

    private HBox filaBase(String clase) {
        HBox h = new HBox(12);
        h.getStyleClass().add(clase);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ---- Fabricas de celdas (reutilizables por las vistas) -----------------

    /** Etiqueta de celda con la clase base "grid-cell" y una clase extra opcional. */
    public static Label celda(String texto, String claseExtra) {
        Label l = new Label(texto);
        l.getStyleClass().add("grid-cell");
        if (claseExtra != null) {
            l.getStyleClass().add(claseExtra);
        }
        return l;
    }

    /** Badge de estado de una suscripcion; {@code null} = "Sin suscripcion". */
    public static Label badge(Suscripcion s) {
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

    /** Boton-icono cuadrado (32x32) con un path SVG monocromo (color via CSS). */
    public static Button botonIcono(String svg, boolean peligro) {
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

    /** Boton "pastilla" pequeno para acciones en fila (Abonar/Cancelar). */
    public static Button botonPill(String texto, boolean primario) {
        Button b = new Button(texto);
        b.getStyleClass().addAll("btn-sm", primario ? "btn-sm-primary" : "btn-sm-secondary");
        return b;
    }
}
