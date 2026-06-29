package pe.com.hospedajeRB.reservas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.reservas.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
