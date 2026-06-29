package pe.com.hospedajeRB.habitaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.hospedajeRB.habitaciones.entity.Habitacion;

import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    // Trae habitaciones con su tipo y estado, ordenadas por numero.
    @Query("select h from Habitacion h order by h.numeroCuarto")
    List<Habitacion> listarOrdenadas();
}
