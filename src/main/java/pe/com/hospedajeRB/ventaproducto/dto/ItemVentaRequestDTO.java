package pe.com.hospedajeRB.ventaproducto.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemVentaRequestDTO(
        @NotNull Long idProducto,
        @NotNull @Positive Integer cantidad
) {
}
