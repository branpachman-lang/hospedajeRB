package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.hospedajeRB.ventaproducto.entity.EstadoComprobante;

import java.util.Optional;

public interface EstadoComprobanteVentaRepository extends JpaRepository<EstadoComprobante, Long> {
    @Query("select e from EstadoComprobante e where upper(e.nombreEstado) = upper(?1)")
    Optional<EstadoComprobante> buscarPorNombre(String nombre);
}
