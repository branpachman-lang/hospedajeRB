package pe.com.hospedajeRB.habitaciones.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Tipo de habitacion (tabla: tipo_habitacion). Precio base por tipo (RF02/RF57). */
@Entity
@Table(name = "tipo_habitacion")
@Getter
@Setter
public class TipoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_habitacion")
    private Long idTipoHabitacion;

    @Column(name = "nombre_tipo", nullable = false, length = 30)
    private String nombreTipo;

    @Column(name = "capacidad_max", nullable = false)
    private Integer capacidadMax;

    @Column(name = "precio_base", nullable = false)
    private BigDecimal precioBase;
}
