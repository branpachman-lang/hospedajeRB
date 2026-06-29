package pe.com.hospedajeRB.seguridad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.seguridad.entity.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
}
