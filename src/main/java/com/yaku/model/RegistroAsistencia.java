package com.yaku.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegistroAsistencia {

    private int id;
    private String estudianteId;
    private LocalDate fecha;
    private LocalTime hora;

    public RegistroAsistencia(int id, String estudianteId, LocalDate fecha, LocalTime hora) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    @Override
    public String toString() {
        return String.format("Asistencia #%d | Estudiante: %s | Fecha: %s | Hora: %s",
                id, estudianteId, fecha, hora);
    }
}
