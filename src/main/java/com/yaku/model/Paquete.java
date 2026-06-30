package com.yaku.model;

import java.util.List;

/**
 * Paquete de clases que un estudiante puede abonar a su saldo de suscripcion.
 *
 * El precio es solo informativo (en soles) — no existe logica de pago. Los
 * paquetes estandar ofrecen un mejor precio por clase que el personalizado.
 */
public record Paquete(String nombre, int clases, double precio) {

    /** Precio por clase de un paquete personalizado (sin descuento), en soles. */
    public static final double PRECIO_POR_CLASE = 15.0;

    /** Catalogo de paquetes estandar. */
    public static List<Paquete> catalogo() {
        return List.of(
                new Paquete("Estandar 12", 12, 120.0),  // S/ 10.00 por clase
                new Paquete("Estandar 24", 24, 200.0)   // S/  8.33 por clase
        );
    }

    /** Construye un paquete personalizado con la cantidad de clases indicada. */
    public static Paquete personalizado(int clases) {
        return new Paquete("Personalizado", clases, clases * PRECIO_POR_CLASE);
    }
}
