package com.yaku.repository;

import com.yaku.db.ConexionDB;
import com.yaku.model.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion del repositorio de estudiantes sobre MySQL (JDBC).
 * Solo persistencia: ni reglas de negocio ni mensajes por consola.
 */
public class EstudianteRepositorySQL implements IEstudianteRepository {

    private Connection conexion() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    @Override
    public void guardar(Estudiante e) {
        String sql = "INSERT INTO estudiantes (id, nombre, apellido, telefono) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, e.getId());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellido());
            ps.setString(4, e.getTelefono());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo guardar el estudiante", ex);
        }
    }

    @Override
    public void actualizar(Estudiante e) {
        String sql = "UPDATE estudiantes SET nombre = ?, apellido = ?, telefono = ? WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getTelefono());
            ps.setString(4, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo actualizar el estudiante", ex);
        }
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo eliminar el estudiante", ex);
        }
    }

    @Override
    public Estudiante buscarPorId(String id) {
        String sql = "SELECT * FROM estudiantes WHERE id = ?";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo buscar el estudiante", ex);
        }
    }

    @Override
    public List<Estudiante> listarTodos() {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY apellido, nombre";
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo listar los estudiantes", ex);
        }
        return lista;
    }

    @Override
    public String obtenerUltimoId() {
        String sql = "SELECT id FROM estudiantes ORDER BY id DESC LIMIT 1";
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString("id") : null;
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo obtener el ultimo id", ex);
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("telefono")
        );
    }
}
