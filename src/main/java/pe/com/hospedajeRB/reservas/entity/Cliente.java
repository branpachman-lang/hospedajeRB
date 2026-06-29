package pe.com.hospedajeRB.reservas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Cliente (tabla: cliente). Comparte PK con tercero. Aqui solo el nombre para mostrar. */
@Entity
@Table(name = "cliente")
@Getter
@Setter
public class Cliente {

    @Id
    @Column(name = "id_tercero")
    private Long idTercero;

    @Column(name = "tipo_cliente", nullable = false, length = 20)
    private String tipoCliente = "Natural";

    @Column(name = "razon_social_nombre", nullable = false, length = 150)
    private String razonSocialNombre;

    @Column(name = "direccion_fiscal", length = 200)
    private String direccionFiscal;
}
