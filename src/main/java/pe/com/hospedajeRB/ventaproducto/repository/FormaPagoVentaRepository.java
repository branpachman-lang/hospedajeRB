package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.hospedajeRB.ventaproducto.entity.FormaPago;

import java.util.List;
import java.util.Optional;

public interface FormaPagoVentaRepository extends JpaRepository<FormaPago, Long> {

    List<FormaPago> findAllByOrderByNombrePagoAsc();

    @Query("select f from FormaPago f where upper(f.nombrePago) = upper(?1)")
    Optional<FormaPago> buscarPorNombre(String nombre);
}
