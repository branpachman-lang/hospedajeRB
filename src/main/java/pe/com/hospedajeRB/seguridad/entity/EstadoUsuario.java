package pe.com.hospedajeRB.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Catalogo de estados de usuario (Activo/Inactivo). RF47 / RF48. */
@Entity
@Table(name = "estado_usuario")
@Getter
@Setter
public class EstadoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_usuario")
    private Long idEstadoUsuario;

    @Column(name = "nombre_estado_usuario", nullable = false, length = 30)
    private String nombreEstadoUsuario;

    public boolean esActivo() {
        return nombreEstadoUsuario != null
                && nombreEstadoUsuario.trim().equalsIgnoreCase("Activo");
    }
}
