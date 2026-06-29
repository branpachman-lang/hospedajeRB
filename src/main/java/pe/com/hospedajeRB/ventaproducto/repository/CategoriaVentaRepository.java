package pe.com.hospedajeRB.ventaproducto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.ventaproducto.entity.Categoria;

import java.util.List;

public interface CategoriaVentaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNombreCategoriaAsc();
}
