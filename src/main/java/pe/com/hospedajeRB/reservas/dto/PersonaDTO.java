package pe.com.hospedajeRB.reservas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/** Datos de una persona (cliente o huesped) en el formulario de reserva. */
public record PersonaDTO(
        String nombres,
        String apellidos,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento,
        String direccion,
        Long idTipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono
) {}
