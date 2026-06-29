package pe.com.hospedajeRB.reservas.dto;

import java.time.LocalDate;

/** Una reserva para pintar en el calendario semanal. */
public record ReservaCalendarioDTO(
        Long id,
        Long idHabitacion,
        String numeroHabitacion,
        String cliente,
        String estado,
        String color,      // naranja | azul | verde | gris | rojo
        LocalDate ingreso,
        LocalDate salida
) {}
