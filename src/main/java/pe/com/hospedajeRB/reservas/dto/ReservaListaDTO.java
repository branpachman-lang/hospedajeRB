package pe.com.hospedajeRB.reservas.dto;

/** Fila del listado "Lista de Reservas" (agrupada por codigo). */
public record ReservaListaDTO(
        String codigo,
        String codigoFormateado,
        String cliente,
        int habitaciones,
        int huespedes,
        boolean pagada,        // si ya tiene comprobante / no es eliminable
        String estado
) {}
