package pe.com.hospedajeRB.reservas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.com.hospedajeRB.habitaciones.entity.Habitacion;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Reserva (tabla: reserva). Para el calendario: cliente, habitacion, fechas y estado. */
@Entity
@Table(name = "reserva")
@Getter
@Setter
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;

    @Column(name = "costo_estadia", nullable = false)
    private BigDecimal costoEstadia;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_reserva", nullable = false)
    private EstadoReserva estadoReserva;

    @Column(name = "id_tercero_usuario", nullable = false)
    private Long idTerceroUsuario;

    @Column(name = "codigo_reserva", length = 12)
    private String codigoReserva;
}
