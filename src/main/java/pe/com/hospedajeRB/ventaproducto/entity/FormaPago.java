package pe.com.hospedajeRB.ventaproducto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "forma_pago")
@Getter
@Setter
public class FormaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_forma_pago")
    private Long idFormaPago;

    @Column(name = "nombre_pago", nullable = false, length = 30)
    private String nombrePago;
}
