package pe.com.hospedajeRB.ventaproducto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComprobanteVentaDTO(
        Long idComprobante,
        String serie,
        String numeroComprobante,
        LocalDateTime fechaEmision,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total,
        boolean cargadoHabitacion,
        ReservaVentaDTO reserva,
        List<ItemComprobanteDTO> items
) {
}
