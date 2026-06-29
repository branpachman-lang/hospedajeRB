package pe.com.hospedajeRB.reservas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Estado de reserva (tabla: estado_reserva): Confirmada, Check-in Pendiente, En curso, Finalizada, Cancelada. */
@Entity
@Table(name = "estado_reserva")
@Getter
@Setter
public class EstadoReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_reserva")
    private Long idEstadoReserva;

    @Column(name = "nombre_estado_reserva", nullable = false, length = 30)
    private String nombreEstadoReserva;
}
