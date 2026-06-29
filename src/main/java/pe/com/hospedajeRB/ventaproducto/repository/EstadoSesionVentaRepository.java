package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.hospedajeRB.ventaproducto.entity.EstadoSesion;

import java.util.Optional;

public interface EstadoSesionVentaRepository extends JpaRepository<EstadoSesion, Long> {
    @Query("select e from EstadoSesion e where upper(e.nombreEstadoSesion) = upper(?1)")
    Optional<EstadoSesion> buscarPorNombre(String nombre);
}
