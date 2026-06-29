package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.reservas.entity.ReservaHuesped;

import java.util.List;

public interface ReservaHuespedVentaRepository extends JpaRepository<ReservaHuesped, ReservaHuesped.PK> {

    @Query("""
           select trim(concat(concat(p.nombres, ' '), p.apellidos))
           from ReservaHuesped rh, Persona p
           where rh.idTercero = p.idTercero
             and rh.idReserva = :idReserva
           order by rh.idTipoOcupante, p.apellidos, p.nombres
           """)
    List<String> nombresPorReserva(@Param("idReserva") Long idReserva);
}