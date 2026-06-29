package pe.com.hospedajeRB.seguridad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.seguridad.entity.Tercero;

import java.util.Optional;

public interface TerceroRepository extends JpaRepository<Tercero, Long> {

    // RF41: recuperacion por correo (anti-enumeracion).
    Optional<Tercero> findFirstByCorreoIgnoreCase(String correo);

    // RF06: buscar cliente/huesped por documento al registrar reserva.
    Optional<Tercero> findFirstByNumeroDocumento(String numeroDocumento);
}
