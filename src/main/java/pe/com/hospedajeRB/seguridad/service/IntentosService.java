package pe.com.hospedajeRB.seguridad.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.seguridad.entity.Usuario;
import pe.com.hospedajeRB.seguridad.repository.UsuarioRepository;

import java.time.LocalDateTime;

/**
 * RN08 / RNF04: control de intentos fallidos y bloqueo temporal de la cuenta.
 * Persiste en usuario.intentos_fallidos y usuario.fecha_bloqueo.
 */
@Service
public class IntentosService {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.seguridad.max-intentos:3}")
    private int maxIntentos;

    public IntentosService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Suma un intento fallido; al llegar al limite, marca el bloqueo. */
    @Transactional
    public void registrarFallo(String username) {
        usuarioRepository.findByUsername(username).ifPresent(u -> {
            int intentos = (u.getIntentosFallidos() == null ? 0 : u.getIntentosFallidos()) + 1;
            u.setIntentosFallidos(intentos);
            if (intentos >= maxIntentos) {
                u.setFechaBloqueo(LocalDateTime.now());
            }
            usuarioRepository.save(u);
        });
    }

    /** Login correcto: limpia contador y desbloquea. */
    @Transactional
    public void registrarExito(String username) {
        usuarioRepository.findByUsername(username).ifPresent(u -> {
            u.setIntentosFallidos(0);
            u.setFechaBloqueo(null);
            usuarioRepository.save(u);
        });
    }
}
