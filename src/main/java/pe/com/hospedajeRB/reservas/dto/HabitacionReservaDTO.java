package pe.com.hospedajeRB.reservas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/** Una habitacion dentro de la reserva, con sus fechas y huespedes. */
public record HabitacionReservaDTO(
        Long idHabitacion,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate checkIn,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate checkOut,
        List<PersonaDTO> huespedes
) {}
