package pe.com.hospedajeRB.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

/** Vistas de inicio por rol (4 roles). Cada una protegida por su rol (RF40/RNF06). */
@Controller
public class DashboardController {

    private void comun(UsuarioPrincipal principal, Model model) {
        model.addAttribute("nombre", principal.getNombreParaMostrar());
    }

    @GetMapping("/gerente")
    public String gerente(@AuthenticationPrincipal UsuarioPrincipal p, Model m) {
        comun(p, m); return "dashboard/gerente";
    }

    @GetMapping("/recepcionista")
    public String recepcionista(@AuthenticationPrincipal UsuarioPrincipal p, Model m) {
        comun(p, m); return "dashboard/recepcionista";
    }

    @GetMapping("/limpieza")
    public String limpieza(@AuthenticationPrincipal UsuarioPrincipal p, Model m) {
        comun(p, m); return "dashboard/limpieza";
    }

    @GetMapping("/mantenimiento")
    public String mantenimiento(@AuthenticationPrincipal UsuarioPrincipal p, Model m) {
        comun(p, m); return "dashboard/mantenimiento";
    }
}
