package com.yaku.repository;

import com.yaku.db.ConexionDB;
import com.yaku.model.RegistroAsistencia;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion del repositorio de asistencias sobre MySQL (JDBC).
 * Solo persistencia: ni reglas de negocio ni mensajes por consola.
 */
public class AsistenciaRepositorySQL implements IAsistenciaRepository {

    private Connection conexion() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    @Override
    public void guardar(RegistroAsistencia a) {
        String sql = "INSERT INTO asistencias (estudiante_id, fecha, hora) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getEstudianteId());
            ps.setDate(2, Date.valueOf(a.getFecha()));
            ps.setTime(3, Time.valueOf(a.getHora()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo guardar la asistencia", e);
        }
    }

    @Override
    public List<RegistroAsistencia> listarPorFecha(LocalDate fecha) {
        List<RegistroAsistencia> lista = new ArrayList<>();
        String sql = "SELECT * FROM asistencias WHERE fecha = ? ORDER BY hora";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new RegistroAsistencia(
                        rs.getInt("id"),
                        rs.getString("estudiante_id"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getTime("hora").toLocalTime()
                ));
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar las asistencias", e);
        }
        return lista;
    }
}
