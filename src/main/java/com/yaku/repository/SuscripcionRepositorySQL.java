package com.yaku.repository;

import com.yaku.db.ConexionDB;
import com.yaku.model.EstadoSuscripcion;
import com.yaku.model.Suscripcion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion del repositorio de suscripciones sobre base de datos (JDBC).
 * Solo persistencia: ni reglas de negocio ni mensajes por consola.
 */
public class SuscripcionRepositorySQL implements ISuscripcionRepository {

    private Connection conexion() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    @Override
    public void guardar(Suscripcion s) {
        String sql = "INSERT INTO suscripciones (estudiante_id, clases_restantes, fecha_inicio, estado) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getEstudianteId());
            ps.setInt(2, s.getClasesRestantes());
            ps.setDate(3, Date.valueOf(s.getFechaInicio()));
            ps.setString(4, s.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo guardar la suscripcion", e);
        }
    }

    @Override
    public Suscripcion buscarPorEstudiante(String estudianteId) {
        String sql = "SELECT * FROM suscripciones WHERE estudiante_id = ? LIMIT 1";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, estudianteId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar la suscripcion", e);
        }
    }

    @Override
    public List<Suscripcion> listarTodas() {
        List<Suscripcion> lista = new ArrayList<>();
        String sql = "SELECT * FROM suscripciones";
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo listar las suscripciones", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Suscripcion s) {
        String sql = "UPDATE suscripciones SET clases_restantes = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setInt(1, s.getClasesRestantes());
            ps.setString(2, s.getEstado().name());
            ps.setInt(3, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo actualizar la suscripcion", e);
        }
    }

    private Suscripcion mapear(ResultSet rs) throws SQLException {
        return new Suscripcion(
                rs.getInt("id"),
                rs.getString("estudiante_id"),
                rs.getInt("clases_restantes"),
                rs.getDate("fecha_inicio").toLocalDate(),
                EstadoSuscripcion.valueOf(rs.getString("estado"))
        );
    }
}
