package pe.com.hospedajeRB.seguridad.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.hospedajeRB.seguridad.entity.Usuario;
import pe.com.hospedajeRB.seguridad.repository.UsuarioRepository;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

import java.util.regex.Pattern;

/**
 * RF36/RF37/RF38/RF39: cambio obligatorio de la contrasena temporal.
 * Tras guardar, cierra la sesion (request.logout) y manda al login para entrar
 * de nuevo ya con la credencial permanente.
 */
@Controller
public class CambioPasswordController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern SEGURA =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    public CambioPasswordController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cambio-password")
    public String form() {
        return "cambio-password";
    }

    @PostMapping("/cambio-password")
    @Transactional
    public String procesar(@AuthenticationPrincipal UsuarioPrincipal principal,
                           @RequestParam String nueva,
                           @RequestParam String confirmacion,
                           HttpServletRequest request,
                           Model model) throws ServletException {

        if (!nueva.equals(confirmacion)) {
            model.addAttribute("error", "Las contrasenas no coinciden.");
            return "cambio-password";
        }
        if (!SEGURA.matcher(nueva).matches()) {
            model.addAttribute("error", "Debe tener al menos 8 caracteres, con letras y numeros.");
            return "cambio-password";
        }

        Usuario u = usuarioRepository.findById(principal.getIdTercero()).orElseThrow();
        u.setPassword(passwordEncoder.encode(nueva));   // RNF03
        u.setCredencialTemporal(0);                     // RF39: ya es permanente
        usuarioRepository.save(u);

        request.logout();                               // cierra la sesion actual
        return "redirect:/login?passwordCambiado";      // vuelve al inicio de sesion
    }
}
