package pe.com.hospedajeRB.reservas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.reservas.dto.ReservaCalendarioDTO;
import pe.com.hospedajeRB.reservas.entity.Reserva;
import pe.com.hospedajeRB.reservas.repository.ReservaRepository;

import java.time.LocalDate;
import java.util.List;

/** Lectura de reservas para el calendario semanal. */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservaCalendarioDTO> calendario(LocalDate desde, LocalDate hasta) {
        return reservaRepository.enRango(desde, hasta).stream()
                .map(this::toDTO)
                .toList();
    }

    private ReservaCalendarioDTO toDTO(Reserva r) {
        String estado = r.getEstadoReserva().getNombreEstadoReserva();
        return new ReservaCalendarioDTO(
                r.getIdReserva(),
                r.getHabitacion().getIdHabitacion(),
                r.getHabitacion().getNumeroCuarto(),
                r.getCliente().getRazonSocialNombre(),
                estado,
                colorEstado(estado),
                r.getFechaIngreso(),
                r.getFechaSalida()
        );
    }

    /** Color de la barra de reserva en el calendario. */
    public static String colorEstado(String nombre) {
        if (nombre == null) return "gris";
        String n = nombre.toLowerCase();
        if (n.contains("confirmad"))  return "naranja";
        if (n.contains("check-out"))  return "morado";
        if (n.contains("check-in"))   return "azul";
        if (n.contains("curso"))      return "verde";
        if (n.contains("finaliz"))    return "gris";
        if (n.contains("cancel"))     return "rojo";
        return "azul";
    }
}
