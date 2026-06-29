package pe.com.hospedajeRB.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Muestra el login. El POST /login lo procesa Spring Security. */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String expirada,
                        @RequestParam(required = false) String reset,
                        @RequestParam(required = false) String passwordCambiado,
                        Model model) {
        model.addAttribute("errorTipo", error);
        model.addAttribute("logout", logout != null);
        model.addAttribute("expirada", expirada != null);
        model.addAttribute("reset", reset != null);
        model.addAttribute("passwordCambiado", passwordCambiado != null);
        return "login";
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/cargando";
    }
}
