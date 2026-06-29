package pe.com.hospedajeRB.habitaciones.dto;

import java.math.BigDecimal;

/** Datos de una habitacion para la lista/tarjetas y filas del calendario. */
public record HabitacionDTO(
        Long id,
        String numero,
        int piso,
        String tipo,
        Long idTipo,
        int capacidad,
        BigDecimal precio,
        String descripcion,
        String estado,
        Long idEstado,
        String color      // verde | naranja | rojo | amarillo | gris (codigo de colores)
) {}
