package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.ventaproducto.entity.Comprobante;

import java.util.List;

public interface ComprobanteVentaRepository extends JpaRepository<Comprobante, Long> {

    @Query("select count(c) from Comprobante c where c.serie = :serie and c.numeroComprobante like :pref")
    long contarDelDia(@Param("serie") String serie, @Param("pref") String pref);

    @Query("""
           select c from Comprobante c
           where c.reserva.idReserva in :ids
             and upper(c.estadoComprobante.nombreEstado) = 'EMITIDO'
             and upper(c.tipoComprobante.nombreComprobante) <> 'NOTA DE CREDITO'
             and upper(c.tipoComprobante.nombreComprobante) <> 'NOTA DE CRÉDITO'
           order by c.idComprobante desc
           """)
    List<Comprobante> vigentesPorReservas(@Param("ids") List<Long> ids);
}
