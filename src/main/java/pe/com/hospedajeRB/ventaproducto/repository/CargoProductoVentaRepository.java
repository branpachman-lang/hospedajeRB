package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.ventaproducto.entity.CargoProducto;

import java.util.List;

public interface CargoProductoVentaRepository extends JpaRepository<CargoProducto, Long> {

    @Query("select cp from CargoProducto cp join fetch cp.producto where cp.reserva.idReserva in :ids and cp.comprobante is null")
    List<CargoProducto> pendientesPorReservas(@Param("ids") List<Long> ids);

    @Query("select cp from CargoProducto cp join fetch cp.producto where cp.comprobante.idComprobante = :idComp")
    List<CargoProducto> porComprobante(@Param("idComp") Long idComp);
}
