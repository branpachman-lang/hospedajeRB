package pe.com.hospedajeRB.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Datos personales (tabla: persona). Usado para el usuario logueado y para huespedes. */
@Entity
@Table(name = "persona")
@Getter
@Setter
public class Persona {

    @Id
    @Column(name = "id_tercero")
    private Long idTercero;

    @Column(name = "nombres", nullable = false, length = 50)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 50)
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    public String nombreCompleto() {
        return (nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos);
    }
}
