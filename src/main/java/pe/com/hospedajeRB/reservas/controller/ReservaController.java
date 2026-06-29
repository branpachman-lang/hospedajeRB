package pe.com.hospedajeRB.reservas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pe.com.hospedajeRB.reservas.repository.TipoDocumentoRepository;

/** Paginas del modulo Registrar Reserva: lista y formulario (alta/edicion). */
@Controller
public class ReservaController {

    private final TipoDocumentoRepository tipoDocumentoRepo;

    public ReservaController(TipoDocumentoRepository tipoDocumentoRepo) {
        this.tipoDocumentoRepo = tipoDocumentoRepo;
    }

    @GetMapping("/recepcionista/reservas")
    public String lista() {
        return "recepcion/reservas";
    }

    @GetMapping("/recepcionista/reservas/nueva")
    public String nueva(Model model) {
        model.addAttribute("tiposDocumento", tipoDocumentoRepo.findAll());
        model.addAttribute("codigoEditar", "");   // alta
        return "recepcion/reserva-form";
    }

    @GetMapping("/recepcionista/reservas/editar/{codigo}")
    public String editar(@PathVariable String codigo, Model model) {
        model.addAttribute("tiposDocumento", tipoDocumentoRepo.findAll());
        model.addAttribute("codigoEditar", codigo); // edicion: el JS carga el detalle
        return "recepcion/reserva-form";
    }
}
