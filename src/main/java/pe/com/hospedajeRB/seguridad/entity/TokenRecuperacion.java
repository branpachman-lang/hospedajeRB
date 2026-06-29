package pe.com.hospedajeRB.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Token de recuperacion (tabla: token_recuperacion). RF42 expira; RF44 usado evita reuso. */
@Entity
@Table(name = "token_recuperacion")
@Getter
@Setter
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;

    @Column(name = "id_tercero", nullable = false)
    private Long idTercero;

    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private Integer usado = 0;

    public boolean estaVigente() {
        return usado == 0 && fechaExpiracion != null
                && fechaExpiracion.isAfter(LocalDateTime.now());
    }
}
