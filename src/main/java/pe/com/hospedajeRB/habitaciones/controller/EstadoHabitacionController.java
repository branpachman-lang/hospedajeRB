package pe.com.hospedajeRB.habitaciones.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.com.hospedajeRB.habitaciones.repository.EstadoHabitacionRepository;
import pe.com.hospedajeRB.habitaciones.repository.TipoHabitacionRepository;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

/** Pagina "Estado de las Habitaciones" del panel de recepcion. */
@Controller
public class EstadoHabitacionController {

    private final TipoHabitacionRepository tipoRepo;
    private final EstadoHabitacionRepository estadoRepo;

    public EstadoHabitacionController(TipoHabitacionRepository tipoRepo,
                                      EstadoHabitacionRepository estadoRepo) {
        this.tipoRepo = tipoRepo;
        this.estadoRepo = estadoRepo;
    }

    @GetMapping("/recepcionista/habitaciones")
    public String habitaciones(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("nombre", principal != null ? principal.getNombreParaMostrar() : "");
        model.addAttribute("tipos", tipoRepo.findAll());     // para el filtro por tipo
        model.addAttribute("estados", estadoRepo.findAll()); // para el filtro por estado
        return "recepcion/habitaciones";
    }
}
