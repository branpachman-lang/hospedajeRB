package pe.com.hospedajeRB.comprobantes.dto;

import java.math.BigDecimal;

public record LineaTicketDTO(String descripcion, int cantidad, BigDecimal precio, BigDecimal importe) {
}
