package pe.com.hospedajeRB.reservas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Catalogo tipo_documento (DNI, etc.) para el filtro del formulario. */
@Entity
@Table(name = "tipo_documento")
@Getter
@Setter
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Long idTipoDocumento;

    @Column(name = "nombre_documento", nullable = false, length = 30)
    private String nombreDocumento;
}
