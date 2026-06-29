package pe.com.hospedajeRB.seguridad.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.seguridad.entity.Usuario;
import pe.com.hospedajeRB.seguridad.repository.UsuarioRepository;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

import java.time.LocalDateTime;

/**
 * RF35: carga el usuario desde Oracle y construye el UserDetails.
 * Tambien resuelve si el bloqueo temporal por intentos (RN08) sigue vigente.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.seguridad.minutos-bloqueo:15}")
    private long minutosBloqueo;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));

        boolean noBloqueada = !bloqueoVigente(u.getFechaBloqueo());
        return new UsuarioPrincipal(u, noBloqueada);
    }

    /** El bloqueo expira pasados los minutos configurados. */
    private boolean bloqueoVigente(LocalDateTime fechaBloqueo) {
        if (fechaBloqueo == null) return false;
        return fechaBloqueo.plusMinutes(minutosBloqueo).isAfter(LocalDateTime.now());
    }
}
