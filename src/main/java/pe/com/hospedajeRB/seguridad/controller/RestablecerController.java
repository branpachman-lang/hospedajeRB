package pe.com.hospedajeRB.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.hospedajeRB.seguridad.service.RecuperacionService;

import java.util.regex.Pattern;

/**
 * RF44: pantalla a la que llega el usuario desde el enlace del correo.
 * Valida el token y permite guardar la nueva contrasena encriptada.
 */
@Controller
public class RestablecerController {

    private static final Pattern SEGURA =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final RecuperacionService recuperacionService;

    public RestablecerController(RecuperacionService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @GetMapping("/restablecer")
    public String form(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("tokenValido", recuperacionService.tokenValido(token));
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String procesar(@RequestParam String token,
                           @RequestParam String nueva,
                           @RequestParam String confirmacion,
                           Model model) {
        model.addAttribute("token", token);

        if (!recuperacionService.tokenValido(token)) {
            model.addAttribute("tokenValido", false);
            return "restablecer";
        }
        model.addAttribute("tokenValido", true);

        if (!nueva.equals(confirmacion)) {
            model.addAttribute("error", "Las contrasenas no coinciden.");
            return "restablecer";
        }
        if (!SEGURA.matcher(nueva).matches()) {
            model.addAttribute("error", "Debe tener al menos 8 caracteres, con letras y numeros.");
            return "restablecer";
        }

        boolean ok = recuperacionService.restablecer(token, nueva);
        if (!ok) {
            model.addAttribute("tokenValido", false);
            return "restablecer";
        }
        // Listo: vuelve al login con aviso de exito.
        return "redirect:/login?reset";
    }
}
