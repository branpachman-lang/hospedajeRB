package pe.com.hospedajeRB.reservas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.reservas.entity.ReservaHuesped;

import java.util.List;

public interface ReservaHuespedRepository extends JpaRepository<ReservaHuesped, ReservaHuesped.PK> {

    List<ReservaHuesped> findByIdReserva(Long idReserva);

    void deleteByIdReserva(Long idReserva);

    long countByIdReserva(Long idReserva);
}
