package pe.com.hospedajeRB.seguridad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.seguridad.entity.TokenRecuperacion;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByToken(String token);
}
