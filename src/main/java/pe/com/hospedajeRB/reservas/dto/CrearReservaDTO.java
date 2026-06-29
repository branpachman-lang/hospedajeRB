package pe.com.hospedajeRB.reservas.dto;

import java.util.List;

/** Payload para crear/editar una reserva: datos del cliente + habitaciones. */
public record CrearReservaDTO(
        PersonaDTO cliente,
        List<HabitacionReservaDTO> habitaciones
) {}
