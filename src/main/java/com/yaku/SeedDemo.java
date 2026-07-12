package com.yaku;

import com.yaku.db.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Carga datos de demostracion en la base H2 local (./data/yaku).
 *
 * Inserta 10 alumnos con sus suscripciones en estados variados (activas con
 * saldo alto/bajo, agotada, cancelada y uno sin suscripcion) y algunas
 * asistencias de hoy, de forma que todas las pantallas de la GUI se vean
 * pobladas como el prototipo.
 *
 * OJO: primero BORRA los datos existentes (estudiantes/suscripciones/
 * asistencias) para dejar un estado limpio y predecible. Ejecutar con:
 *
 *   mvn -q compile exec:java@seed
 */
public class SeedDemo {

    // id, nombre, apellido, telefono
    private static final String[][] ALUMNOS = {
            {"EST001", "Mateo",     "Quispe",    "+51 987 654 321"},
            {"EST002", "Valentina", "Rojas",     "+51 954 118 902"},
            {"EST003", "Diego",     "Fernández", "+51 921 447 655"},
            {"EST004", "Camila",    "Torres",    "+51 998 210 774"},
            {"EST005", "Sebastián", "Vargas",    "+51 936 552 108"},
            {"EST006", "Luciana",   "Castro",    "+51 942 883 019"},
            {"EST007", "Thiago",    "Mendoza",   "+51 915 706 244"},
            {"EST008", "Isabella",  "Ríos",      "+51 967 330 581"},
            {"EST009", "Adriano",   "Salazar",   "+51 903 619 470"},
            {"EST010", "Antonella", "Chávez",    "+51 989 002 156"},
    };

    // estudiante_id, clases_restantes, estado, fecha_inicio
    private static final Object[][] SUSCRIPCIONES = {
            {"EST001",  8, "ACTIVA",    "2026-05-14"},
            {"EST002",  2, "ACTIVA",    "2026-06-02"},
            {"EST003", 24, "ACTIVA",    "2026-04-20"},
            {"EST004",  0, "AGOTADA",   "2026-03-11"},
            {"EST005",  3, "ACTIVA",    "2026-06-18"},
            {"EST006", 15, "ACTIVA",    "2026-05-30"},
            {"EST007",  0, "CANCELADA", "2026-02-08"},
            {"EST008",  1, "ACTIVA",    "2026-06-25"},
            {"EST009", 11, "ACTIVA",    "2026-05-05"},
            // EST010 (Antonella) queda SIN suscripcion a proposito.
    };

    // estudiante_id, hora (fecha = hoy)
    private static final String[][] ASISTENCIAS_HOY = {
            {"EST003", "08:15"},
            {"EST001", "09:02"},
            {"EST009", "10:30"},
            {"EST006", "11:45"},
    };

    public static void main(String[] args) throws Exception {
        Connection conn = ConexionDB.getInstancia().getConexion();  // crea el esquema si no existe

        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM asistencias");
            st.execute("DELETE FROM suscripciones");
            st.execute("DELETE FROM estudiantes");
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO estudiantes (id, nombre, apellido, telefono) VALUES (?, ?, ?, ?)")) {
            for (String[] a : ALUMNOS) {
                ps.setString(1, a[0]);
                ps.setString(2, a[1]);
                ps.setString(3, a[2]);
                ps.setString(4, a[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO suscripciones (estudiante_id, clases_restantes, fecha_inicio, estado) "
                        + "VALUES (?, ?, ?, ?)")) {
            for (Object[] s : SUSCRIPCIONES) {
                ps.setString(1, (String) s[0]);
                ps.setInt(2, (Integer) s[1]);
                ps.setDate(3, Date.valueOf(LocalDate.parse((String) s[3])));
                ps.setString(4, (String) s[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO asistencias (estudiante_id, fecha, hora) VALUES (?, ?, ?)")) {
            Date hoy = Date.valueOf(LocalDate.now());
            for (String[] a : ASISTENCIAS_HOY) {
                ps.setString(1, a[0]);
                ps.setDate(2, hoy);
                ps.setTime(3, Time.valueOf(LocalTime.parse(a[1])));
                ps.addBatch();
            }
            ps.executeBatch();
        }

        System.out.printf(
                "Datos de demostracion cargados: %d alumnos, %d suscripciones, %d asistencias de hoy.%n",
                ALUMNOS.length, SUSCRIPCIONES.length, ASISTENCIAS_HOY.length);
    }
}
