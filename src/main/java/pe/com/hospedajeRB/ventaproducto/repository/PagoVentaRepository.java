package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.ventaproducto.entity.Pago;

public interface PagoVentaRepository extends JpaRepository<Pago, Long> {
}
