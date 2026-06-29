package pe.com.hospedajeRB.comprobantes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.hospedajeRB.comprobantes.dto.ComprobanteResultDTO;
import pe.com.hospedajeRB.comprobantes.dto.EmitirComprobanteRequestDTO;
import pe.com.hospedajeRB.comprobantes.dto.ResumenPagoDTO;
import pe.com.hospedajeRB.comprobantes.service.ComprobanteService;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;
import pe.com.hospedajeRB.ventaproducto.dto.FormaPagoVentaDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recepcionista/api/comprobante")
public class ComprobanteApiController {

    private final ComprobanteService service;

    public ComprobanteApiController(ComprobanteService service) {
        this.service = service;
    }

    @GetMapping("/formas-pago")
    public List<FormaPagoVentaDTO> formasPago() {
        return service.listarFormasPago();
    }

    @GetMapping("/tipos-comprobante")
    public List<String> tiposComprobante() {
        return service.listarTiposComprobante();
    }

    @GetMapping("/resumen/{codigo}")
    public ResumenPagoDTO resumen(@PathVariable String codigo) {
        return service.resumen(codigo);
    }

    @PostMapping("/emitir/{codigo}")
    public Map<String, Object> emitir(@PathVariable String codigo,
                                      @RequestBody EmitirComprobanteRequestDTO req,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        ComprobanteResultDTO r = service.emitir(codigo, req, principal.getIdTercero());
        return Map.of("ok", true, "comprobante", r);
    }

    @PostMapping("/anular/{codigo}")
    public Map<String, Object> anular(@PathVariable String codigo,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        ComprobanteResultDTO r = service.anular(codigo, principal.getIdTercero());
        return Map.of("ok", true, "comprobante", r);
    }

    @GetMapping("/vigente/{codigo}")
    public Map<String, Object> vigente(@PathVariable String codigo) {
        Long id = service.comprobanteVigente(codigo);
        return id == null ? Map.of("ok", false) : Map.of("ok", true, "idComprobante", id);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> manejarError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "mensaje", e.getMessage()));
    }
}
