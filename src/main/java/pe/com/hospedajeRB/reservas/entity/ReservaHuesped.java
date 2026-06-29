package pe.com.hospedajeRB.reservas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/** Huesped de una reserva (tabla puente reserva_huesped). PK compuesta (id_reserva, id_tercero). */
@Entity
@Table(name = "reserva_huesped")
@IdClass(ReservaHuesped.PK.class)
@Getter
@Setter
public class ReservaHuesped {

    @Id
    @Column(name = "id_reserva")
    private Long idReserva;

    @Id
    @Column(name = "id_tercero")
    private Long idTercero;

    @Column(name = "id_tipo_ocupante", nullable = false)
    private Long idTipoOcupante;

    /** Clave compuesta. */
    @Getter
    @Setter
    public static class PK implements Serializable {
        private Long idReserva;
        private Long idTercero;

        public PK() {}
        public PK(Long idReserva, Long idTercero) { this.idReserva = idReserva; this.idTercero = idTercero; }

        @Override public boolean equals(Object o){
            if(this==o) return true;
            if(!(o instanceof PK pk)) return false;
            return Objects.equals(idReserva, pk.idReserva) && Objects.equals(idTercero, pk.idTercero);
        }
        @Override public int hashCode(){ return Objects.hash(idReserva, idTercero); }
    }
}
