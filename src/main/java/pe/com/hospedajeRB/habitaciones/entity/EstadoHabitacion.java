package pe.com.hospedajeRB.habitaciones.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Estado de habitacion (tabla: estado_habitacion): Disponible, Reservada, Ocupada, En limpieza, En mantenimiento. */
@Entity
@Table(name = "estado_habitacion")
@Getter
@Setter
public class EstadoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_habitacion")
    private Long idEstadoHabitacion;

    @Column(name = "nombre_estado", nullable = false, length = 30)
    private String nombreEstado;
}
