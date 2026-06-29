package pe.com.hospedajeRB.habitaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.habitaciones.entity.TipoHabitacion;

public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {
}
