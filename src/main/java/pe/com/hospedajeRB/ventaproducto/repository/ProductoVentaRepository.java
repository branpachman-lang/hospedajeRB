package pe.com.hospedajeRB.ventaproducto.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.ventaproducto.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoVentaRepository extends JpaRepository<Producto, Long> {

    @Query("""
           select p from Producto p
           join fetch p.categoria c
           join fetch p.estado e
           where (:categoriaId is null or c.idCategoria = :categoriaId)
             and (:q is null or lower(p.nombreProducto) like lower(concat(concat('%', :q), '%')))
             and lower(e.nombreEstado) <> 'descontinuado'
           order by c.nombreCategoria, p.nombreProducto
           """)
    List<Producto> buscarCatalogo(@Param("categoriaId") Long categoriaId, @Param("q") String q);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p join fetch p.categoria join fetch p.estado where p.idProducto = :id")
    Optional<Producto> bloquearPorId(@Param("id") Long id);
}
