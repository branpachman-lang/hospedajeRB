package pe.com.hospedajeRB.ventaproducto.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;
import pe.com.hospedajeRB.ventaproducto.dto.*;
import pe.com.hospedajeRB.ventaproducto.service.VentaProductoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recepcionista/api/venta-producto")
public class VentaProductoApiController {

    private final VentaProductoService service;

    public VentaProductoApiController(VentaProductoService service) {
        this.service = service;
    }

    @GetMapping("/categorias")
    public List<CategoriaVentaDTO> categorias() {
        return service.listarCategorias();
    }

    @GetMapping("/productos")
    public List<ProductoVentaDTO> productos(@RequestParam(required = false) Long categoriaId,
                                            @RequestParam(required = false) String q) {
        return service.listarProductos(categoriaId, q);
    }

    @GetMapping("/formas-pago")
    public List<FormaPagoVentaDTO> formasPago() {
        return service.listarFormasPago();
    }

    @GetMapping("/reservas")
    public List<ReservaVentaDTO> buscarReservas(@RequestParam String q) {
        return service.buscarReservas(q);
    }

    @PostMapping("/confirmar")
    public Map<String, Object> confirmar(@Valid @RequestBody ConfirmarVentaRequestDTO request,
                                         @AuthenticationPrincipal UsuarioPrincipal principal) {
        return Map.of("ok", true, "comprobante", service.confirmar(request, principal.getIdTercero()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> manejarError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "mensaje", e.getMessage()));
    }
}
