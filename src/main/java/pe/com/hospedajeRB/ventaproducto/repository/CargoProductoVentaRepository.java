package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.ventaproducto.entity.CargoProducto;

public interface CargoProductoVentaRepository extends JpaRepository<CargoProducto, Long> {
}
