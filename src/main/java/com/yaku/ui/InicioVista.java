package com.yaku.ui;

import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Estudiante;
import com.yaku.model.RegistroAsistencia;
import com.yaku.model.Suscripcion;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.SuscripcionService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Pantalla "Inicio": dashboard que resume el resto de modulos (rediseno Claude
 * Design). Agrega alumnos, suscripciones y asistencias en metricas, dos listas
 * (por renovar / asistencias de hoy) y una barra de distribucion de estados.
 *
 * Es solo lectura + accesos rapidos: los botones y enlaces navegan a la pantalla
 * correspondiente (se los inyecta {@code navegar}).
 */
public class InicioVista extends VBox {

    private static final Locale ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", ES_PE);
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EstudianteService estudiantes;
    private final SuscripcionService suscripciones;
    private final AsistenciaService asistencias;
    private final Consumer<String> navegar;

    public InicioVista(EstudianteService estudiantes,
                       SuscripcionService suscripciones,
                       AsistenciaService asistencias,
                       Consumer<String> navegar) {
        this.estudiantes = estudiantes;
        this.suscripciones = suscripciones;
        this.asistencias = asistencias;
        this.navegar = navegar;

        // Datos agregados
        List<Estudiante> alumnos = estudiantes.listarTodos();
        List<Suscripcion> subs = suscripciones.listarTodas();
        List<RegistroAsistencia> hoy = asistencias.listarDelDia();
        int activas = (int) subs.stream().filter(s -> s.getEstado() == EstadoSuscripcion.ACTIVA).count();
        int agotadas = (int) subs.stream().filter(s -> s.getEstado() == EstadoSuscripcion.AGOTADA).count();
        int canceladas = (int) subs.stream().filter(s -> s.getEstado() == EstadoSuscripcion.CANCELADA).count();
        int sinSusc = Math.max(0, alumnos.size() - subs.size());
        List<Suscripcion> porRenovar = suscripciones.listarConSaldoBajo();

        setSpacing(0);
        VBox.setVgrow(this, Priority.NEVER);

        HBox encabezado = construirEncabezado();
        Region metricas = construirMetricas(alumnos.size(), activas, hoy.size(), porRenovar.size());
        HBox columnas = construirColumnas(porRenovar, hoy);
        Region distribucion = construirDistribucion(alumnos.size(), activas, agotadas, canceladas, sinSusc);

        getChildren().addAll(encabezado, metricas, columnas, distribucion);
        VBox.setMargin(encabezado, new Insets(0, 0, 22, 0));
        VBox.setMargin(metricas, new Insets(0, 0, 20, 0));
        VBox.setMargin(distribucion, new Insets(16, 0, 0, 0));
    }

    // ---- Encabezado --------------------------------------------------------

    private HBox construirEncabezado() {
        Label titulo = new Label(saludo());
        titulo.getStyleClass().add("h1");
        titulo.setStyle("-fx-font-size:25px;");
        Label sub = new Label("Resumen operativo de la academia · " + fechaLarga());
        sub.getStyleClass().add("subtitle");
        VBox textos = new VBox(5, titulo, sub);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Button regAlumno = new Button("+ Registrar alumno");
        regAlumno.getStyleClass().add("btn-secondary");
        regAlumno.setOnAction(e -> navegar.accept("Estudiantes"));

        Button regAsist = new Button("Registrar asistencia");
        regAsist.getStyleClass().add("btn-primary");
        regAsist.setOnAction(e -> navegar.accept("Asistencias"));

        HBox acciones = new HBox(10, regAlumno, regAsist);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(16, textos, esp, acciones);
        fila.setAlignment(Pos.BOTTOM_LEFT);
        return fila;
    }

    private String saludo() {
        int h = LocalTime.now().getHour();
        if (h < 12) {
            return "Buenos días";
        } else if (h < 19) {
            return "Buenas tardes";
        }
        return "Buenas noches";
    }

    private String fechaLarga() {
        String s = LocalDate.now().format(FMT_FECHA);
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ---- Metricas ----------------------------------------------------------

    private Region construirMetricas(int alumnos, int activas, int asistHoy, int porRenovar) {
        HBox fila = new HBox(15,
                metrica("#0f4c5c", "ALUMNOS REGISTRADOS", String.valueOf(alumnos), "en la academia", false),
                metrica("#2a9d8f", "SUSCRIPCIONES ACTIVAS", String.valueOf(activas), "con clases disponibles", false),
                metrica("#3d7ea6", "ASISTENCIAS DE HOY", String.valueOf(asistHoy), "clases dictadas", false),
                metrica("#d99b3c", "POR RENOVAR", String.valueOf(porRenovar),
                        "saldo ≤ " + SuscripcionService.UMBRAL_SALDO_BAJO + " clases", true));
        return fila;
    }

    private Region metrica(String colorDot, String etiqueta, String valor, String sub, boolean valorAmbar) {
        Region dot = new Region();
        dot.setMinSize(8, 8);
        dot.setMaxSize(8, 8);
        dot.setStyle("-fx-background-color:" + colorDot + "; -fx-background-radius:50;");
        Label cap = new Label(etiqueta);
        cap.getStyleClass().add("metric-cap");
        HBox top = new HBox(8, dot, cap);
        top.setAlignment(Pos.CENTER_LEFT);

        Label val = new Label(valor);
        val.getStyleClass().add("dash-value");
        if (valorAmbar) {
            val.getStyleClass().add("dash-value-amber");
        }
        Label s = new Label(sub);
        s.getStyleClass().add("dash-sub");

        VBox card = new VBox(top, val, s);
        card.getStyleClass().add("dash-card");
        VBox.setMargin(top, new Insets(0, 0, 13, 0));
        VBox.setMargin(s, new Insets(5, 0, 0, 0));
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ---- Columnas (por renovar / asistencias de hoy) -----------------------

    private HBox construirColumnas(List<Suscripcion> porRenovar, List<RegistroAsistencia> hoy) {
        VBox izq = construirPorRenovar(porRenovar);
        VBox der = construirAsistenciasHoy(hoy);
        HBox.setHgrow(izq, Priority.ALWAYS);
        HBox.setHgrow(der, Priority.ALWAYS);
        izq.setMaxWidth(Double.MAX_VALUE);
        der.setMaxWidth(Double.MAX_VALUE);
        return new HBox(16, izq, der);
    }

    private VBox construirPorRenovar(List<Suscripcion> porRenovar) {
        Label h = new Label("Suscripciones por renovar");
        h.getStyleClass().add("card-title");
        Label conteo = new Label(String.valueOf(porRenovar.size()));
        conteo.getStyleClass().addAll("badge", "badge-agotada");
        HBox izqHead = new HBox(9, h, conteo);
        izqHead.setAlignment(Pos.CENTER_LEFT);

        Label link = new Label("Ver suscripciones →");
        link.getStyleClass().add("link");
        link.setOnMouseClicked(e -> navegar.accept("Suscripciones"));

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox head = new HBox(izqHead, esp, link);
        head.getStyleClass().add("list-card-head");
        head.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(head);
        card.getStyleClass().add("list-card");

        if (porRenovar.isEmpty()) {
            card.getChildren().add(vacio("Todo en orden. Ninguna suscripción con saldo bajo."));
        } else {
            for (Suscripcion s : porRenovar) {
                Estudiante e = estudiantes.buscarPorId(s.getEstudianteId());
                card.getChildren().add(filaPorRenovar(e, s));
            }
        }
        return card;
    }

    private HBox filaPorRenovar(Estudiante e, Suscripcion s) {
        Region dot = new Region();
        dot.setMinSize(9, 9);
        dot.setMaxSize(9, 9);
        dot.setStyle("-fx-background-color:#d99b3c; -fx-background-radius:50;");

        String nombreTxt = e != null ? e.getNombre() + " " + e.getApellido() : s.getEstudianteId();
        Label nombre = new Label(nombreTxt);
        nombre.setStyle("-fx-font-weight:600; -fx-font-size:14px;");
        Label meta = new Label(s.getEstudianteId() + " · " + estadoLabel(s.getEstado()));
        meta.getStyleClass().add("cell-id");
        meta.setStyle("-fx-font-size:12px;");
        VBox centro = new VBox(nombre, meta);
        centro.setMinWidth(0);
        HBox.setHgrow(centro, Priority.ALWAYS);

        Label clases = new Label(String.valueOf(s.getClasesRestantes()));
        clases.setStyle("-fx-font-family:'IBM Plex Mono','Consolas',monospace; -fx-font-weight:500; -fx-font-size:15px; -fx-text-fill:#a8722a;");
        Label clasesCap = new Label("clases");
        clasesCap.setStyle("-fx-font-size:11px; -fx-text-fill:#8a999d;");
        VBox saldo = new VBox(clases, clasesCap);
        saldo.setAlignment(Pos.CENTER_RIGHT);

        Button abonar = TablaGrid.botonPill("Abonar", false);
        abonar.setStyle("-fx-text-fill:#0f4c5c;");
        abonar.setOnAction(ev -> navegar.accept("Suscripciones"));

        HBox fila = new HBox(14, dot, centro, saldo, abonar);
        fila.getStyleClass().add("list-row");
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private VBox construirAsistenciasHoy(List<RegistroAsistencia> hoy) {
        Label h = new Label("Asistencias de hoy");
        h.getStyleClass().add("card-title");
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        Label conteo = new Label(String.valueOf(hoy.size()));
        conteo.getStyleClass().add("grid-titlebar-count");
        HBox head = new HBox(h, esp, conteo);
        head.getStyleClass().add("list-card-head");
        head.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(head);
        card.getStyleClass().add("list-card");

        if (hoy.isEmpty()) {
            card.getChildren().add(vacio("Aún no hay asistencias registradas hoy."));
        } else {
            List<RegistroAsistencia> ordenadas = hoy.stream()
                    .sorted(Comparator.comparing(RegistroAsistencia::getHora))
                    .toList();
            for (RegistroAsistencia a : ordenadas) {
                Estudiante e = estudiantes.buscarPorId(a.getEstudianteId());
                card.getChildren().add(filaAsistencia(e, a));
            }
        }
        return card;
    }

    private HBox filaAsistencia(Estudiante e, RegistroAsistencia a) {
        Label avatar = new Label(iniciales(e));
        avatar.getStyleClass().add("avatar-sm");

        String nombreTxt = e != null ? e.getNombre() + " " + e.getApellido() : a.getEstudianteId();
        Label nombre = new Label(nombreTxt);
        nombre.setStyle("-fx-font-weight:500; -fx-font-size:13.5px;");
        Label id = new Label(a.getEstudianteId());
        id.getStyleClass().add("cell-id");
        id.setStyle("-fx-font-size:11.5px;");
        VBox centro = new VBox(nombre, id);
        centro.setMinWidth(0);
        HBox.setHgrow(centro, Priority.ALWAYS);

        Label hora = new Label(a.getHora().format(FMT_HORA));
        hora.setStyle("-fx-font-family:'IBM Plex Mono','Consolas',monospace; -fx-font-size:12.5px; -fx-text-fill:#5a6b70;");

        HBox fila = new HBox(12, avatar, centro, hora);
        fila.getStyleClass().add("list-row");
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    // ---- Distribucion ------------------------------------------------------

    private Region construirDistribucion(int total, int activas, int agotadas, int canceladas, int sinSusc) {
        Label h = new Label("Distribución de suscripciones");
        h.getStyleClass().add("card-title");

        double base = total <= 0 ? 1 : total;
        double a = activas / base * 100;
        double b = a + agotadas / base * 100;
        double c = b + canceladas / base * 100;
        Region barra = new Region();
        barra.getStyleClass().add("dist-bar");
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setStyle(String.format(Locale.US,
                "-fx-background-color: linear-gradient(to right, "
                + "#2a9d8f 0%%, #2a9d8f %1$.4f%%, #d99b3c %1$.4f%%, #d99b3c %2$.4f%%, "
                + "#c3cccf %2$.4f%%, #c3cccf %3$.4f%%, #eef2f3 %3$.4f%%, #eef2f3 100%%);",
                a, b, c));

        Region espLeyenda = new Region();
        HBox.setHgrow(espLeyenda, Priority.ALWAYS);
        HBox leyenda = new HBox(22,
                itemLeyenda("#2a9d8f", "Activas", activas, false),
                itemLeyenda("#d99b3c", "Agotadas", agotadas, false),
                itemLeyenda("#c3cccf", "Canceladas", canceladas, false),
                espLeyenda,
                itemLeyenda("#e6ebec", "Sin suscripción", sinSusc, true));
        leyenda.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(15, h, barra, leyenda);
        card.getStyleClass().add("panel");
        return card;
    }

    private HBox itemLeyenda(String color, String nombre, int cantidad, boolean conBorde) {
        Region sw = new Region();
        sw.setMinSize(10, 10);
        sw.setMaxSize(10, 10);
        String estilo = "-fx-background-color:" + color + "; -fx-background-radius:2;";
        if (conBorde) {
            estilo += " -fx-border-color:#cdd8da; -fx-border-radius:2;";
        }
        sw.setStyle(estilo);
        Label n = new Label(nombre);
        n.getStyleClass().add("legend-name");
        Label c = new Label(String.valueOf(cantidad));
        c.getStyleClass().add("legend-count");
        HBox item = new HBox(7, sw, n, c);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    // ---- Auxiliares --------------------------------------------------------

    private Label vacio(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("muted");
        l.setStyle("-fx-font-size:13.5px;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        l.setPadding(new Insets(34, 20, 34, 20));
        return l;
    }

    private String estadoLabel(EstadoSuscripcion estado) {
        return switch (estado) {
            case ACTIVA -> "Activa";
            case AGOTADA -> "Agotada";
            case CANCELADA -> "Cancelada";
        };
    }

    private String iniciales(Estudiante e) {
        if (e == null) {
            return "—";
        }
        String n = e.getNombre().isBlank() ? "" : e.getNombre().substring(0, 1);
        String a = e.getApellido().isBlank() ? "" : e.getApellido().substring(0, 1);
        String r = (n + a).toUpperCase(ES_PE);
        return r.isBlank() ? "—" : r;
    }
}
