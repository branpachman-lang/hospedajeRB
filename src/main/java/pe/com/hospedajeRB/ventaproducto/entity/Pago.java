package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter
@Setter
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_comprobante", nullable = false)
    private Comprobante comprobante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_forma_pago", nullable = false)
    private FormaPago formaPago;

    @Column(name = "id_tercero_usuario", nullable = false)
    private Long idTerceroUsuario;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "numero_operacion", length = 50)
    private String numeroOperacion;

    @Column(name = "verificado", nullable = false)
    private Integer verificado = 1;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;
}
