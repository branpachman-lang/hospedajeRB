package pe.com.hospedajeRB.ventaproducto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaVentaDTO(
        Long idReserva,
        String codigoReserva,
        String cliente,
        String habitacion,
        String tipoHabitacion,
        String descripcionHabitacion,
        LocalDate fechaIngreso,
        LocalDate fechaSalida,
        BigDecimal costoEstadia
) {
}
