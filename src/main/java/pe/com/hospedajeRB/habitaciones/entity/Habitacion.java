package pe.com.hospedajeRB.habitaciones.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Habitacion (tabla: habitacion). El piso se deriva del numero_cuarto. */
@Entity
@Table(name = "habitacion")
@Getter
@Setter
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Long idHabitacion;

    @Column(name = "numero_cuarto", nullable = false, length = 10)
    private String numeroCuarto;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoHabitacion tipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoHabitacion estado;

    /** Piso derivado del numero (ej. "201" -> 2). */
    @Transient
    public int getPiso() {
        try {
            String n = numeroCuarto.replaceAll("[^0-9]", "");
            if (n.length() <= 2) return 0;
            return Integer.parseInt(n.substring(0, n.length() - 2));
        } catch (Exception e) {
            return 0;
        }
    }
}
