package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estado_sesion")
@Getter
@Setter
public class EstadoSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_sesion")
    private Long idEstadoSesion;

    @Column(name = "nombre_estado_sesion", nullable = false, length = 30)
    private String nombreEstadoSesion;
}
