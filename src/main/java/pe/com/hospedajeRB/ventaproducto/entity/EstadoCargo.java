package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estado_cargo")
@Getter
@Setter
public class EstadoCargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_cargo")
    private Long idEstadoCargo;

    @Column(name = "nombre_estado", nullable = false, length = 30)
    private String nombreEstado;
}
