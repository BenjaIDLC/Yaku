package com.yaku.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexion unica (Singleton) a la base de datos embebida H2.
 *
 * Los datos se guardan en un archivo local (carpeta ./data), por lo que no
 * requiere instalar ni configurar ningun servidor: la aplicacion funciona en
 * cualquier equipo y los datos persisten entre ejecuciones. Al conectarse se
 * crea el esquema (schema.sql) si aun no existe.
 */
public class ConexionDB {

    private static final String URL = "jdbc:h2:./data/yaku;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static ConexionDB instancia;
    private Connection conexion;

    private ConexionDB() throws SQLException {
        this.conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        inicializarEsquema();
    }

    public static ConexionDB getInstancia() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    /** Ejecuta schema.sql (sentencias CREATE TABLE IF NOT EXISTS, idempotentes). */
    private void inicializarEsquema() throws SQLException {
        try (InputStream in = getClass().getResourceAsStream("/schema.sql")) {
            if (in == null) {
                return;
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = conexion.createStatement()) {
                for (String sentencia : script.split(";")) {
                    if (!sentencia.isBlank()) {
                        st.execute(sentencia);
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("No se pudo leer schema.sql para inicializar la base de datos", e);
        }
    }
}
