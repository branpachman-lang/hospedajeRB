package pe.com.hospedajeRB.seguridad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.hospedajeRB.seguridad.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // RF35: autenticacion buscando por username. Tambien permite usar correo
    // (ver UsuarioDetailsService) si quieres login por correo (RF41).
    Optional<Usuario> findByUsername(String username);
}
