package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estado_producto")
@Getter
@Setter
public class EstadoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_producto")
    private Long idEstadoProducto;

    @Column(name = "nombre_estado", nullable = false, length = 30)
    private String nombreEstado;
}
