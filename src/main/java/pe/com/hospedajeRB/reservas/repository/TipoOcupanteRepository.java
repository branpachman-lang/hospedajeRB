package pe.com.hospedajeRB.reservas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.reservas.entity.TipoOcupante;

public interface TipoOcupanteRepository extends JpaRepository<TipoOcupante, Long> {
}
