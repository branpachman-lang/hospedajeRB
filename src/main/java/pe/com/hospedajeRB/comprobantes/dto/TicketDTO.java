package pe.com.hospedajeRB.comprobantes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TicketDTO(
        String empresa,
        String rucEmpresa,
        String direccionEmpresa,
        String tipo,
        String serie,
        String numero,
        LocalDateTime fecha,
        String cliente,
        String docTipo,
        String docNumero,
        List<LineaTicketDTO> estadia,
        List<LineaTicketDTO> productos,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total,
        String formaPago,
        String cajero,
        boolean anulado
) {
}
