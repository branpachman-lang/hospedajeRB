package pe.com.hospedajeRB.comprobantes.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenPagoDTO(
        String codigo,
        String cliente,
        String numeroDocumento,
        String tipoDocumento,
        boolean esRuc,
        List<LineaResumenDTO> habitaciones,
        int totalHuespedes,
        List<LineaResumenDTO> cargos,
        BigDecimal totalEstadia,
        BigDecimal totalCargos,
        BigDecimal total,
        boolean yaPagada
) {
}
