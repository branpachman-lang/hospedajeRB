package pe.com.hospedajeRB.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Usuario del sistema (tabla: usuario). PK id_tercero encadena empleado->persona->tercero.
 *  password=BCrypt(RNF03), credencial_temporal(RF36/37/39), intentos_fallidos+fecha_bloqueo(RN08),
 *  estado_usuario(RF47/48), roles via empleado_rol (RF40/RBAC).
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    @Id
    @Column(name = "id_tercero")
    private Long idTercero;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "credencial_temporal", nullable = false)
    private Integer credencialTemporal = 1;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_usuario", nullable = false)
    private EstadoUsuario estadoUsuario;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "empleado_rol",
            joinColumns = @JoinColumn(name = "id_tercero"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> roles = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tercero", insertable = false, updatable = false)
    private Persona persona;

    public boolean esTemporal() {
        return credencialTemporal != null && credencialTemporal == 1;
    }
}
