package pe.com.hospedajeRB.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

/**
 * Pantalla de carga intermedia tras el login.
 * Calcula la vista destino segun el rol y la entrega a la plantilla;
 * el JS de cargando.html espera un instante y redirige alli.
 */
@Controller
public class CargandoController {

    @GetMapping("/cargando")
    public String cargando(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("destino", RolHome.resolver(principal));
        model.addAttribute("nombre", principal.getNombreParaMostrar());
        return "cargando";   // templates/cargando.html
    }
}
