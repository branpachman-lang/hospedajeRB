package pe.com.hospedajeRB.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Catalogo de roles (tabla: rol). Autoridad de Spring Security = ROLE_ + nombreRol. */
@Entity
@Table(name = "rol")
@Getter
@Setter
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol", nullable = false, length = 30)
    private String nombreRol;
}
