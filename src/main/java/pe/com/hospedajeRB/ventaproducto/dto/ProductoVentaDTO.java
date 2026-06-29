package pe.com.hospedajeRB.ventaproducto.dto;

import java.math.BigDecimal;

public record ProductoVentaDTO(
        Long idProducto,
        String nombreProducto,
        String nombreCategoria,
        BigDecimal precioActual,
        Integer stock,
        Integer stockMinimo,
        boolean disponible,
        boolean stockBajo
) {
}
