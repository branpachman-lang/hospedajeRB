package pe.com.hospedajeRB.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.hospedajeRB.seguridad.service.RecuperacionService;

/**
 * RF41-RF43: solicitud de recuperacion por correo. Por respuesta uniforme NO
 * revelamos si el correo existe (evita enumeracion de usuarios).
 */
@Controller
public class RecuperarController {

    private final RecuperacionService recuperacionService;

    public RecuperarController(RecuperacionService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @GetMapping("/recuperar")
    public String form() {
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String enviar(@RequestParam String correo, Model model) {
        recuperacionService.solicitar(correo);   // RF42: token 15 min + RF43: correo
        model.addAttribute("enviado", true);
        return "recuperar";
    }
}
