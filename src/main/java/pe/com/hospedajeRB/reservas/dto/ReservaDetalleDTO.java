package pe.com.hospedajeRB.reservas.dto;

import java.util.List;

/** Reserva completa para precargar el formulario de edicion. */
public record ReservaDetalleDTO(
        String codigo,
        PersonaDTO cliente,
        List<HabitacionReservaDTO> habitaciones
) {}
