package pe.com.hospedajeRB.reservas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Catalogo tipo_ocupante (Titular, Acompanante). */
@Entity
@Table(name = "tipo_ocupante")
@Getter
@Setter
public class TipoOcupante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_ocupante")
    private Long idTipoOcupante;

    @Column(name = "nombre_tipo", nullable = false, length = 30)
    private String nombreTipo;
}
