package pe.com.hospedajeRB.ventaproducto.dto;

import java.math.BigDecimal;

public record ItemComprobanteDTO(
        String producto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal total
) {
}
