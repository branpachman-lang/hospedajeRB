package pe.com.hospedajeRB.habitaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.habitaciones.entity.EstadoHabitacion;

import java.util.Optional;

public interface EstadoHabitacionRepository extends JpaRepository<EstadoHabitacion, Long> {

    Optional<EstadoHabitacion> findFirstByNombreEstadoIgnoreCase(String nombreEstado);
}
