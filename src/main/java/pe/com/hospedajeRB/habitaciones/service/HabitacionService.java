package pe.com.hospedajeRB.habitaciones.service;

import java.util.List;
import pe.com.hospedajeRB.habitaciones.dto.HabitacionDTO;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface HabitacionService {

    List<HabitacionDTO> listar(Integer piso, Long idTipo, Long idEstado);

    void cambiarEstado(Long idHabitacion, Long idEstado);

}
