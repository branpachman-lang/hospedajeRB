package pe.com.hospedajeRB.reservas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.hospedajeRB.reservas.entity.Reserva;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Reservas que se solapan con el rango [desde, hasta] (para pintar el calendario).
    @Query("""
           select r from Reserva r
           where r.fechaIngreso <= :hasta and r.fechaSalida >= :desde
           order by r.habitacion.numeroCuarto, r.fechaIngreso
           """)
    List<Reserva> enRango(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    // Cuenta reservas de la MISMA habitacion cuyas fechas se cruzan con [checkIn, checkOut).
    // Intervalos medio-abiertos: el dia de salida puede coincidir con el de entrada de otra.
    // Excluye la propia reserva (por codigo) para permitir la edicion.
    @Query("""
           select count(r) from Reserva r
           where r.habitacion.idHabitacion = :idHab
             and (:codigo is null or r.codigoReserva <> :codigo)
             and r.fechaIngreso < :checkOut
             and r.fechaSalida > :checkIn
           """)
    long contarSolapadas(@Param("idHab") Long idHab,
                         @Param("checkIn") LocalDate checkIn,
                         @Param("checkOut") LocalDate checkOut,
                         @Param("codigo") String codigo);

    // Todas las filas de una reserva logica (mismo codigo)
    List<Reserva> findByCodigoReservaOrderByIdReserva(String codigoReserva);

    // Listado (cada fila es una habitacion; se agrupa por codigo en el servicio)
    List<Reserva> findAllByOrderByCodigoReservaDescIdReserva();

    // Correlativo del dia: cuantos codigos distintos empiezan con el prefijo AAMMDD
    @Query("select count(distinct r.codigoReserva) from Reserva r where r.codigoReserva like :prefijo")
    long contarCodigosDelDia(@Param("prefijo") String prefijo);

    // Filtro por estado de reserva (Check-in Pendiente / Check-out Pendiente)
    List<Reserva> findByEstadoReserva_NombreEstadoReservaOrderByCodigoReserva(String nombreEstado);
}
