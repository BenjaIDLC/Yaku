package com.yaku.view;

import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Estudiante;
import com.yaku.model.Paquete;
import com.yaku.model.RegistroAsistencia;
import com.yaku.model.Suscripcion;
import com.yaku.repository.RepositorioException;
import com.yaku.service.AsistenciaService;
import com.yaku.service.EstudianteService;
import com.yaku.service.SuscripcionService;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ConsolaView {

    private final Scanner scanner = new Scanner(System.in);
    private final EstudianteService estudianteService;
    private final SuscripcionService suscripcionService;
    private final AsistenciaService asistenciaService;

    public ConsolaView(EstudianteService estudianteService,
                       SuscripcionService suscripcionService,
                       AsistenciaService asistenciaService) {
        this.estudianteService = estudianteService;
        this.suscripcionService = suscripcionService;
        this.asistenciaService = asistenciaService;
    }

    public void iniciar() {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opcion");
            System.out.println();
            manejarOpcion(opcion);
            if (opcion != 0) pausar();
        } while (opcion != 0);
    }

    private static final int MENU_ANCHO = 34;

    private void mostrarMenu() {
        String borde = "═".repeat(MENU_ANCHO);
        System.out.println("╔" + borde + "╗");
        System.out.printf("║%-" + MENU_ANCHO + "s║%n", "          SISTEMA YAKU");
        System.out.println("╠" + borde + "╣");
        itemMenu(1, "Registrar estudiante");
        itemMenu(2, "Buscar estudiante por ID");
        itemMenu(3, "Buscar estudiante por nombre");
        itemMenu(4, "Listar estudiantes");
        itemMenu(5, "Editar estudiante");
        itemMenu(6, "Eliminar estudiante");
        itemMenu(7, "Abonar clases (paquete)");
        itemMenu(8, "Ver suscripcion");
        itemMenu(9, "Cancelar suscripcion");
        itemMenu(10, "Registrar asistencia");
        itemMenu(11, "Asistencias del dia");
        itemMenu(12, "Reporte de saldo bajo");
        itemMenu(0, "Salir");
        System.out.println("╚" + borde + "╝");
    }

    private void itemMenu(int numero, String texto) {
        System.out.printf("║%-" + MENU_ANCHO + "s║%n", String.format(" %2d. %s", numero, texto));
    }

    private void manejarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> registrarEstudiante();
            case 2 -> buscarEstudiante();
            case 3 -> buscarEstudiantePorNombre();
            case 4 -> listarEstudiantes();
            case 5 -> editarEstudiante();
            case 6 -> eliminarEstudiante();
            case 7 -> abonarClases();
            case 8 -> verSuscripcion();
            case 9 -> cancelarSuscripcion();
            case 10 -> registrarAsistencia();
            case 11 -> verAsistenciasDelDia();
            case 12 -> reporteSaldoBajo();
            case 0 -> System.out.println("Saliendo del sistema. ¡Hasta luego!");
            default -> System.out.println("Opcion no valida. Intente nuevamente.");
        }
    }

    private void registrarEstudiante() {
        System.out.println("--- Registrar nuevo estudiante ---");
        String nombre = leerTexto("Nombre");
        String apellido = leerTexto("Apellido");
        String telefono = leerTexto("Telefono");
        try {
            Estudiante e = estudianteService.registrar(nombre, apellido, telefono);
            System.out.println("Estudiante registrado exitosamente con ID: " + e.getId());
        } catch (RepositorioException ex) {
            System.out.println("Error al registrar estudiante: " + ex.getMessage());
        }
    }

    private void buscarEstudiante() {
        System.out.println("--- Buscar estudiante por ID ---");
        String id = leerTexto("ID del estudiante (ej: EST001)").toUpperCase();
        try {
            Estudiante e = estudianteService.buscarPorId(id);
            if (e != null) {
                System.out.println(e);
            } else {
                System.out.println("No se encontro ningun estudiante con ID: " + id);
            }
        } catch (RepositorioException ex) {
            System.out.println("Error al buscar estudiante: " + ex.getMessage());
        }
    }

    private void buscarEstudiantePorNombre() {
        System.out.println("--- Buscar estudiante por nombre ---");
        System.out.println("Se busca por apellido (o apellido y nombre), desde el inicio.");
        String texto = leerTexto("Texto a buscar");
        if (texto.isBlank()) {
            System.out.println("Debe ingresar al menos una letra.");
            return;
        }
        try {
            List<Estudiante> resultados = estudianteService.buscarPorNombre(texto);
            if (resultados.isEmpty()) {
                System.out.println("No se encontraron estudiantes que coincidan con: " + texto);
            } else {
                resultados.forEach(System.out::println);
                System.out.println("Total: " + resultados.size() + " coincidencia(s).");
            }
        } catch (RepositorioException ex) {
            System.out.println("Error al buscar estudiante: " + ex.getMessage());
        }
    }

    private void listarEstudiantes() {
        System.out.println("--- Lista de estudiantes ---");
        try {
            List<Estudiante> lista = estudianteService.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("No hay estudiantes registrados.");
            } else {
                lista.forEach(System.out::println);
                System.out.println("Total: " + lista.size() + " estudiante(s).");
            }
        } catch (RepositorioException ex) {
            System.out.println("Error al listar estudiantes: " + ex.getMessage());
        }
    }

    private void editarEstudiante() {
        System.out.println("--- Editar estudiante ---");
        String id = leerTexto("ID del estudiante").toUpperCase();
        try {
            Estudiante actual = estudianteService.buscarPorId(id);
            if (actual == null) {
                System.out.println("No se encontro ningun estudiante con ID: " + id);
                return;
            }
            System.out.println("Estudiante actual: " + actual);
            System.out.println("Deje un campo en blanco para mantener el valor actual.");
            String nombre = leerTexto("Nuevo nombre");
            String apellido = leerTexto("Nuevo apellido");
            String telefono = leerTexto("Nuevo telefono");
            estudianteService.editar(id,
                    nombre.isBlank() ? actual.getNombre() : nombre,
                    apellido.isBlank() ? actual.getApellido() : apellido,
                    telefono.isBlank() ? actual.getTelefono() : telefono);
            System.out.println("Estudiante actualizado correctamente.");
        } catch (RepositorioException e) {
            System.out.println("Error al editar estudiante: " + e.getMessage());
        }
    }

    private void eliminarEstudiante() {
        System.out.println("--- Eliminar estudiante ---");
        String id = leerTexto("ID del estudiante").toUpperCase();
        try {
            switch (estudianteService.eliminar(id)) {
                case NO_EXISTE -> System.out.println("No se encontro ningun estudiante con ID: " + id);
                case TIENE_SUSCRIPCION_ACTIVA -> System.out.println("No se puede eliminar: el estudiante tiene una suscripcion ACTIVA. Cancelela primero.");
                case ELIMINADO -> System.out.println("Estudiante eliminado correctamente.");
            }
        } catch (RepositorioException e) {
            System.out.println("Error al eliminar estudiante: " + e.getMessage());
        }
    }

    private void abonarClases() {
        System.out.println("--- Abonar clases (paquete) ---");
        String id = leerTexto("ID del estudiante").toUpperCase();
        try {
            if (estudianteService.buscarPorId(id) == null) {
                System.out.println("No se encontro ningun estudiante con ID: " + id);
                return;
            }
            List<Paquete> catalogo = Paquete.catalogo();
            System.out.println("Paquetes disponibles:");
            for (int i = 0; i < catalogo.size(); i++) {
                Paquete p = catalogo.get(i);
                System.out.printf("  %d. %-13s -> %2d clases (S/ %.2f)%n", i + 1, p.nombre(), p.clases(), p.precio());
            }
            int opcionCustom = catalogo.size() + 1;
            System.out.printf("  %d. Personalizado    (S/ %.2f por clase)%n", opcionCustom, Paquete.PRECIO_POR_CLASE);

            int sel = leerEntero("Paquete");
            Paquete elegido;
            if (sel >= 1 && sel <= catalogo.size()) {
                elegido = catalogo.get(sel - 1);
            } else if (sel == opcionCustom) {
                int cantidad = leerEntero("Cantidad de clases");
                if (cantidad <= 0) {
                    System.out.println("Cantidad invalida. Debe ser mayor a 0.");
                    return;
                }
                elegido = Paquete.personalizado(cantidad);
            } else {
                System.out.println("Paquete invalido.");
                return;
            }

            Suscripcion s = suscripcionService.abonar(id, elegido.clases());
            System.out.printf("Abonadas %d clases (%s, S/ %.2f). Saldo actual: %d clases.%n",
                    elegido.clases(), elegido.nombre(), elegido.precio(), s.getClasesRestantes());
        } catch (RepositorioException e) {
            System.out.println("Error al abonar clases: " + e.getMessage());
        }
    }

    private void verSuscripcion() {
        System.out.println("--- Ver suscripcion de un estudiante ---");
        String id = leerTexto("ID del estudiante").toUpperCase();
        try {
            Suscripcion s = suscripcionService.obtenerSuscripcion(id);
            if (s != null) {
                System.out.println(s);
                Set<EstadoSuscripcion> siguientes = suscripcionService.siguientesEstados(s.getEstado());
                if (siguientes.isEmpty()) {
                    System.out.println("Puede pasar a: (ningun estado)");
                } else {
                    System.out.println("Puede pasar a: " + siguientes);
                }
            } else {
                System.out.println("El estudiante no tiene suscripcion registrada.");
            }
        } catch (RepositorioException e) {
            System.out.println("Error al consultar suscripcion: " + e.getMessage());
        }
    }

    private void cancelarSuscripcion() {
        System.out.println("--- Cancelar suscripcion ---");
        String id = leerTexto("ID del estudiante").toUpperCase();
        try {
            switch (suscripcionService.cancelar(id)) {
                case NO_EXISTE -> System.out.println("El estudiante no tiene suscripcion registrada.");
                case YA_CANCELADA -> System.out.println("La suscripcion ya estaba cancelada.");
                case CANCELADA -> System.out.println("Suscripcion cancelada. El saldo de clases queda anulado.");
            }
        } catch (RepositorioException e) {
            System.out.println("Error al cancelar suscripcion: " + e.getMessage());
        }
    }

    private void registrarAsistencia() {
        System.out.println("--- Registrar asistencia ---");
        String id = leerTexto("ID del estudiante").toUpperCase();
        try {
            AsistenciaService.Registro r = asistenciaService.registrar(id);
            switch (r.resultado()) {
                case SIN_SUSCRIPCION_ACTIVA -> System.out.println("No se puede registrar asistencia: el estudiante no tiene suscripcion ACTIVA con clases disponibles.");
                case SIN_CLASES -> System.out.println("No se puede registrar asistencia: la suscripcion no tiene clases restantes.");
                case REGISTRADA -> {
                    System.out.printf("Asistencia registrada. Fecha: %s | Hora: %s | Clases restantes: %d%n",
                            r.asistencia().getFecha(), r.asistencia().getHora(), r.clasesRestantes());
                    if (r.agotada()) {
                        System.out.println("Aviso: la suscripcion ha sido marcada como AGOTADA (sin clases restantes).");
                    }
                }
            }
        } catch (RepositorioException e) {
            System.out.println("Error al registrar asistencia: " + e.getMessage());
        }
    }

    private void verAsistenciasDelDia() {
        System.out.println("--- Asistencias registradas hoy ---");
        try {
            List<RegistroAsistencia> lista = asistenciaService.listarDelDia();
            if (lista.isEmpty()) {
                System.out.println("No hay asistencias registradas hoy.");
            } else {
                lista.forEach(System.out::println);
                System.out.println("Total: " + lista.size() + " asistencia(s).");
            }
        } catch (RepositorioException e) {
            System.out.println("Error al obtener asistencias del dia: " + e.getMessage());
        }
    }

    private void reporteSaldoBajo() {
        System.out.println("--- Reporte de suscripciones con saldo bajo (<= "
                + SuscripcionService.UMBRAL_SALDO_BAJO + " clases) ---");
        try {
            List<Suscripcion> lista = suscripcionService.listarConSaldoBajo();
            if (lista.isEmpty()) {
                System.out.println("No hay suscripciones con saldo bajo. Todo en orden.");
            } else {
                for (Suscripcion s : lista) {
                    Estudiante e = estudianteService.buscarPorId(s.getEstudianteId());
                    String nombre = (e != null) ? e.getNombre() + " " + e.getApellido() : "(estudiante desconocido)";
                    System.out.printf("[%s] %-22s | Clases restantes: %2d | Estado: %s%n",
                            s.getEstudianteId(), nombre, s.getClasesRestantes(), s.getEstado());
                }
                System.out.println("Total: " + lista.size() + " suscripcion(es) por renovar.");
            }
        } catch (RepositorioException e) {
            System.out.println("Error al generar el reporte: " + e.getMessage());
        }
    }

    private String leerTexto(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return scanner.nextLine().trim();
    }

    private int leerEntero(String etiqueta) {
        System.out.print(etiqueta + ": ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void pausar() {
        System.out.println();
        System.out.print("Presione ENTER para continuar...");
        scanner.nextLine();
        System.out.println();
    }
}
