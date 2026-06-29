package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipo_comprobante")
@Getter
@Setter
public class TipoComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_comprobante")
    private Long idTipoComprobante;

    @Column(name = "nombre_comprobante", nullable = false, length = 30)
    private String nombreComprobante;
}
