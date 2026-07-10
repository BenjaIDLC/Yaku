package com.yaku.model;

import java.time.LocalDate;

/**
 * Suscripcion de un estudiante: un saldo de clases prepagadas.
 *
 * Las clases se abonan por paquetes y se acumulan; no vencen. El estado refleja
 * el saldo: ACTIVA (clases > 0), AGOTADA (clases == 0) o CANCELADA (anulada).
 */
public class Suscripcion {

    private int id;
    private String estudianteId;
    private int clasesRestantes;
    private LocalDate fechaInicio;
    private EstadoSuscripcion estado;

    public Suscripcion(int id, String estudianteId, int clasesRestantes,
                       LocalDate fechaInicio, EstadoSuscripcion estado) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.clasesRestantes = clasesRestantes;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public int getClasesRestantes() { return clasesRestantes; }
    public void setClasesRestantes(int clasesRestantes) { this.clasesRestantes = clasesRestantes; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public EstadoSuscripcion getEstado() { return estado; }
    public void setEstado(EstadoSuscripcion estado) { this.estado = estado; }

    @Override
    public String toString() {
        return String.format("Suscripcion #%d | Clases restantes: %2d | Desde: %s | Estado: %s",
                id, clasesRestantes, fechaInicio, estado);
    }
}
