package pe.com.hospedajeRB.reservas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.hospedajeRB.reservas.dto.CrearReservaDTO;
import pe.com.hospedajeRB.reservas.dto.ReservaDetalleDTO;
import pe.com.hospedajeRB.reservas.dto.ReservaListaDTO;
import pe.com.hospedajeRB.reservas.service.ReservaCrudService;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

import java.util.List;
import java.util.Map;

/** API (fetch) del CRUD de reservas. */
@RestController
@RequestMapping("/recepcionista/api/crud")
public class ReservaCrudApiController {

    private final ReservaCrudService service;

    public ReservaCrudApiController(ReservaCrudService service) {
        this.service = service;
    }

    @GetMapping("/lista")
    public List<ReservaListaDTO> lista(@RequestParam(required = false) String estado) {
        return service.listar(estado);
    }

    @GetMapping("/buscar")
    public Map<String, Object> buscar(@RequestParam String doc) {
        var p = service.buscarPorDocumento(doc);
        return p == null ? Map.of("found", false) : Map.of("found", true, "persona", p);
    }

    @GetMapping("/r/{codigo}")
    public ReservaDetalleDTO detalle(@PathVariable String codigo) {
        return service.detalle(codigo);
    }

    @PostMapping
    public Map<String, Object> crear(@RequestBody CrearReservaDTO dto,
                                     @AuthenticationPrincipal UsuarioPrincipal principal) {
        String codigo = service.crear(dto, principal.getIdTercero());
        return Map.of("ok", true, "codigo", codigo);
    }

    @PutMapping("/{codigo}")
    public Map<String, Object> editar(@PathVariable String codigo, @RequestBody CrearReservaDTO dto,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        service.editar(codigo, dto, principal.getIdTercero());
        return Map.of("ok", true, "codigo", codigo);
    }

    @DeleteMapping("/{codigo}")
    public Map<String, Object> eliminar(@PathVariable String codigo) {
        service.eliminar(codigo);
        return Map.of("ok", true);
    }

    @PostMapping("/{codigo}/comprobante")
    public Map<String, Object> comprobante(@PathVariable String codigo) {
        return Map.of("ok", true, "mensaje", service.comprobante(codigo));
    }

    // Errores de validacion (capacidad, no eliminable, etc.) -> 400 con mensaje
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> manejarError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "mensaje", e.getMessage()));
    }
}
