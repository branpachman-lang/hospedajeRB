package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.reservas.entity.Reserva;

import java.util.List;
import java.util.Optional;

public interface ReservaVentaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
           select r from Reserva r
           join fetch r.cliente c
           join fetch r.habitacion h
           join fetch h.tipo
           where lower(coalesce(r.codigoReserva, '')) like lower(concat(concat('%', :q), '%'))
              or lower(h.numeroCuarto) like lower(concat(concat('%', :q), '%'))
              or lower(c.razonSocialNombre) like lower(concat(concat('%', :q), '%'))
           order by r.fechaIngreso desc
           """)
    List<Reserva> buscarParaVenta(@Param("q") String q);

    @Query("""
           select r from Reserva r
           join fetch r.cliente c
           join fetch r.habitacion h
           join fetch h.tipo
           where r.idReserva = :id
           """)
    Optional<Reserva> buscarDetalle(@Param("id") Long id);
}
