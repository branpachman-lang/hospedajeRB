package pe.com.hospedajeRB.seguridad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.seguridad.entity.Tercero;
import pe.com.hospedajeRB.seguridad.entity.TokenRecuperacion;
import pe.com.hospedajeRB.seguridad.entity.Usuario;
import pe.com.hospedajeRB.seguridad.repository.TerceroRepository;
import pe.com.hospedajeRB.seguridad.repository.TokenRecuperacionRepository;
import pe.com.hospedajeRB.seguridad.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * RF41-RF44: solicitud, generacion, envio y validacion de tokens de recuperacion.
 */
@Service
public class RecuperacionService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionService.class);

    private final TerceroRepository terceroRepository;
    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    @Value("${app.recuperacion.minutos-token:15}")
    private long minutosToken;
    @Value("${app.mail.from:no-responder@hospedajerb.pe}")
    private String mailFrom;

    public RecuperacionService(TerceroRepository terceroRepository,
                               UsuarioRepository usuarioRepository,
                               TokenRecuperacionRepository tokenRepository,
                               PasswordEncoder passwordEncoder,
                               JavaMailSender mailSender) {
        this.terceroRepository = terceroRepository;
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    /**
     * RF41/RF42/RF43: si el correo corresponde a un usuario, genera un token con
     * expiracion y envia el enlace. No revela si el correo existe (anti-enumeracion).
     */
    @Transactional
    public void solicitar(String correo) {
        Optional<Tercero> tercero = terceroRepository.findFirstByCorreoIgnoreCase(correo);
        if (tercero.isEmpty()) {
            log.info("RF41 - recuperacion solicitada para correo sin coincidencia");
            return;
        }
        Optional<Usuario> usuario = usuarioRepository.findById(tercero.get().getIdTercero());
        if (usuario.isEmpty()) {
            return; // el tercero no es un usuario del sistema
        }

        TokenRecuperacion t = new TokenRecuperacion();
        t.setIdTercero(usuario.get().getIdTercero());
        t.setToken(UUID.randomUUID().toString());
        t.setFechaCreacion(LocalDateTime.now());
        t.setFechaExpiracion(LocalDateTime.now().plusMinutes(minutosToken));
        t.setUsado(0);
        tokenRepository.save(t);

        enviarCorreo(correo, t.getToken());
    }

    private void enviarCorreo(String correo, String token) {
        String enlace = baseUrl + "/restablecer?token=" + token;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(correo);
        msg.setSubject("Recuperacion de contrasena - Hospedaje Real Bolognesi");
        msg.setText("Solicitaste restablecer tu contrasena.\n\n" +
                "Abre este enlace (valido " + minutosToken + " minutos):\n" + enlace +
                "\n\nSi no fuiste tu, ignora este mensaje.");
        try {
            mailSender.send(msg);
            log.info("RF43 - correo de recuperacion enviado");
        } catch (Exception e) {
            // No interrumpe el flujo; queda traza para diagnosticar SMTP.
            log.error("RF43 - fallo el envio del correo de recuperacion: {}", e.getMessage());
        }
    }

    /** RF44: el token debe existir, no estar usado y no haber expirado. */
    @Transactional(readOnly = true)
    public boolean tokenValido(String token) {
        return tokenRepository.findByToken(token)
                .map(TokenRecuperacion::estaVigente)
                .orElse(false);
    }

    /**
     * RF44: valida el token y guarda la nueva contrasena encriptada (RNF03).
     * Devuelve true si se actualizo correctamente.
     */
    @Transactional
    public boolean restablecer(String token, String nuevaPassword) {
        Optional<TokenRecuperacion> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty() || !opt.get().estaVigente()) {
            return false;
        }
        TokenRecuperacion t = opt.get();
        Usuario u = usuarioRepository.findById(t.getIdTercero()).orElseThrow();
        u.setPassword(passwordEncoder.encode(nuevaPassword));
        u.setCredencialTemporal(0);     // queda permanente
        u.setIntentosFallidos(0);
        u.setFechaBloqueo(null);
        usuarioRepository.save(u);

        t.setUsado(1);                  // RF44: el token no se puede reutilizar
        tokenRepository.save(t);
        return true;
    }
}
