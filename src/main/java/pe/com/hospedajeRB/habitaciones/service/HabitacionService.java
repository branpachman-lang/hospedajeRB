package pe.com.hospedajeRB.habitaciones.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.habitaciones.dto.HabitacionDTO;
import pe.com.hospedajeRB.habitaciones.entity.EstadoHabitacion;
import pe.com.hospedajeRB.habitaciones.entity.Habitacion;
import pe.com.hospedajeRB.habitaciones.repository.EstadoHabitacionRepository;
import pe.com.hospedajeRB.habitaciones.repository.HabitacionRepository;

import java.util.List;

/** Logica de "Estado de Habitaciones": listado con filtros y cambio de estado. */
@Service
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final EstadoHabitacionRepository estadoHabitacionRepository;

    public HabitacionService(HabitacionRepository habitacionRepository,
                             EstadoHabitacionRepository estadoHabitacionRepository) {
        this.habitacionRepository = habitacionRepository;
        this.estadoHabitacionRepository = estadoHabitacionRepository;
    }

    @Transactional(readOnly = true)
    public List<HabitacionDTO> listar(Integer piso, Long idTipo, Long idEstado) {
        return habitacionRepository.listarOrdenadas().stream()
                .filter(h -> piso == null || h.getPiso() == piso)
                .filter(h -> idTipo == null || h.getTipo().getIdTipoHabitacion().equals(idTipo))
                .filter(h -> idEstado == null || h.getEstado().getIdEstadoHabitacion().equals(idEstado))
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void cambiarEstado(Long idHabitacion, Long idEstado) {
        Habitacion h = habitacionRepository.findById(idHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("Habitacion no existe"));
        EstadoHabitacion e = estadoHabitacionRepository.findById(idEstado)
                .orElseThrow(() -> new IllegalArgumentException("Estado no existe"));
        h.setEstado(e);
        habitacionRepository.save(h);
    }

    private HabitacionDTO toDTO(Habitacion h) {
        return new HabitacionDTO(
                h.getIdHabitacion(),
                h.getNumeroCuarto(),
                h.getPiso(),
                h.getTipo().getNombreTipo(),
                h.getTipo().getIdTipoHabitacion(),
                h.getTipo().getCapacidadMax(),
                h.getTipo().getPrecioBase(),
                h.getDescripcion(),
                h.getEstado().getNombreEstado(),
                h.getEstado().getIdEstadoHabitacion(),
                colorEstado(h.getEstado().getNombreEstado())
        );
    }

    /** Codigo de colores RNF07/RNF12 segun el estado de la habitacion. */
    public static String colorEstado(String nombre) {
        if (nombre == null) return "gris";
        String n = nombre.toLowerCase();
        if (n.contains("disponible"))    return "verde";
        if (n.contains("reservad"))      return "naranja";
        if (n.contains("ocupad"))        return "rojo";
        if (n.contains("limpieza"))      return "amarillo";
        if (n.contains("mantenimiento")) return "gris";
        return "azul";
    }
}
