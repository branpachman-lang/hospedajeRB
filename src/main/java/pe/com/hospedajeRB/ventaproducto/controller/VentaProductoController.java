package pe.com.hospedajeRB.ventaproducto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.com.hospedajeRB.ventaproducto.service.VentaProductoService;

@Controller
public class VentaProductoController {

    private final VentaProductoService service;

    public VentaProductoController(VentaProductoService service) {
        this.service = service;
    }

    @GetMapping("/recepcionista/venta-producto")
    public String registrarVenta(Model model) {
        model.addAttribute("categorias", service.listarCategorias());
        return "recepcion/venta-producto";
    }
}
