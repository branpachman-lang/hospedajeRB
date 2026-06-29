package pe.com.hospedajeRB.ventaproducto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConfirmarVentaRequestDTO(
        Long idReserva,
        boolean cargarAHabitacion,
        Long idFormaPago,
        String numeroOperacion,
        @Valid @NotEmpty List<ItemVentaRequestDTO> items
) {
}
