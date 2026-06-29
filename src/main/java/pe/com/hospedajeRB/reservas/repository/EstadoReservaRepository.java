package pe.com.hospedajeRB.reservas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.reservas.entity.EstadoReserva;

import java.util.Optional;

public interface EstadoReservaRepository extends JpaRepository<EstadoReserva, Long> {

    Optional<EstadoReserva> findFirstByNombreEstadoReservaIgnoreCase(String nombre);
}
