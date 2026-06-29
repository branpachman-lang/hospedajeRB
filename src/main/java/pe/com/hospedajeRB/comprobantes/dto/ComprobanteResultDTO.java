package pe.com.hospedajeRB.comprobantes.dto;

import java.math.BigDecimal;

public record ComprobanteResultDTO(Long idComprobante, String serie, String numero, String tipo, BigDecimal total) {
}
