package pe.com.hospedajeRB.comprobantes.dto;

import java.math.BigDecimal;

public record LineaResumenDTO(String descripcion, String detalle, int cantidad, BigDecimal importe) {
}
