package pe.com.hospedajeRB.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tercero (tabla: tercero). Persona/empresa con documento y contacto. */
@Entity
@Table(name = "tercero")
@Getter
@Setter
public class Tercero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tercero")
    private Long idTercero;

    @Column(name = "id_tipo_documento", nullable = false)
    private Long idTipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 50)
    private String numeroDocumento;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "telefono", length = 15)
    private String telefono;
}
