package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.ventaproducto.entity.Comprobante;

public interface ComprobanteVentaRepository extends JpaRepository<Comprobante, Long> {

    @Query("select coalesce(max(c.numeroComprobante), '00000000') from Comprobante c where c.serie = :serie")
    String ultimoNumero(@Param("serie") String serie);
}
