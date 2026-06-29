package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.ventaproducto.entity.TurnoCaja;

import java.util.List;

public interface TurnoCajaVentaRepository extends JpaRepository<TurnoCaja, Long> {

    @Query("""
           select t from TurnoCaja t
           join fetch t.estadoSesion e
           where t.idTerceroUsuario = :idUsuario
             and t.fechaCierre is null
             and upper(e.nombreEstadoSesion) in ('ABIERTO', 'ABIERTA')
           order by t.fechaApertura desc
           """)
    List<TurnoCaja> buscarAbiertosPorUsuario(@Param("idUsuario") Long idUsuario);
}
