package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.com.hospedajeRB.reservas.entity.Reserva;

import java.math.BigDecimal;

@Entity
@Table(name = "cargo_producto")
@Getter
@Setter
public class CargoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargo")
    private Long idCargo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_comprobante")
    private Comprobante comprobante;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_cargo", nullable = false)
    private EstadoCargo estadoCargo;
}
