package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.com.hospedajeRB.reservas.entity.Cliente;
import pe.com.hospedajeRB.reservas.entity.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobante")
@Getter
@Setter
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Long idComprobante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_comprobante", nullable = false)
    private TipoComprobante tipoComprobante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_turno", nullable = false)
    private TurnoCaja turno;

    @Column(name = "serie", nullable = false, length = 10)
    private String serie;

    @Column(name = "numero_comprobante", nullable = false, length = 20)
    private String numeroComprobante;

    @Column(name = "total_pagar", nullable = false)
    private BigDecimal totalPagar;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_comprobante", nullable = false)
    private EstadoComprobante estadoComprobante;
}
