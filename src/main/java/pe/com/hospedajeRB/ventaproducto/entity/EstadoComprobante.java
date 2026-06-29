package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estado_comprobante")
@Getter
@Setter
public class EstadoComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_comprobante")
    private Long idEstadoComprobante;

    @Column(name = "nombre_estado", nullable = false, length = 30)
    private String nombreEstado;
}
