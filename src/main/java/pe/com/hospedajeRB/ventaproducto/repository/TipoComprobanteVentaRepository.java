package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.hospedajeRB.ventaproducto.entity.TipoComprobante;

import java.util.Optional;

public interface TipoComprobanteVentaRepository extends JpaRepository<TipoComprobante, Long> {
    @Query("select t from TipoComprobante t where upper(t.nombreComprobante) = upper(?1)")
    Optional<TipoComprobante> buscarPorNombre(String nombre);
}
