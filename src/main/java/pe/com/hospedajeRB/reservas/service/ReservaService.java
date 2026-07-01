package pe.com.hospedajeRB.reservas.service;

import java.util.List;
import java.time.LocalDate;
import pe.com.hospedajeRB.reservas.dto.ReservaCalendarioDTO;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface ReservaService {

    List<ReservaCalendarioDTO> calendario(LocalDate desde, LocalDate hasta);

}
