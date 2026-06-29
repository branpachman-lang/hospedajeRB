package pe.com.hospedajeRB.reservas.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.com.hospedajeRB.reservas.dto.ReservaCalendarioDTO;
import pe.com.hospedajeRB.reservas.service.ReservaService;

import java.time.LocalDate;
import java.util.List;

/** API (fetch) de reservas para el calendario semanal. */
@RestController
@RequestMapping("/recepcionista/api")
public class ReservaApiController {

    private final ReservaService reservaService;

    public ReservaApiController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/reservas")
    public List<ReservaCalendarioDTO> reservas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reservaService.calendario(desde, hasta);
    }
}
