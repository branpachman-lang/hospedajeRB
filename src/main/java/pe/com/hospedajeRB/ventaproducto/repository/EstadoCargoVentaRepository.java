package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.hospedajeRB.ventaproducto.entity.EstadoCargo;

import java.util.Optional;

public interface EstadoCargoVentaRepository extends JpaRepository<EstadoCargo, Long> {
    @Query("select e from EstadoCargo e where upper(e.nombreEstado) = upper(?1)")
    Optional<EstadoCargo> buscarPorNombre(String nombre);
}
