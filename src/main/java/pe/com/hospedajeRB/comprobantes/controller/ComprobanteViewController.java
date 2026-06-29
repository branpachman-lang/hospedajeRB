package pe.com.hospedajeRB.comprobantes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pe.com.hospedajeRB.comprobantes.service.ComprobanteService;

@Controller
public class ComprobanteViewController {

    private final ComprobanteService service;

    public ComprobanteViewController(ComprobanteService service) {
        this.service = service;
    }

    /** Vista imprimible (ticketera 80mm) del comprobante. */
    @GetMapping("/recepcionista/comprobante/{id}/ticket")
    public String ticket(@PathVariable Long id, Model model) {
        model.addAttribute("t", service.ticket(id));
        return "recepcion/ticket";
    }
}
