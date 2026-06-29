package pe.com.hospedajeRB.habitaciones.controller;

import org.springframework.web.bind.annotation.*;
import pe.com.hospedajeRB.habitaciones.dto.HabitacionDTO;
import pe.com.hospedajeRB.habitaciones.service.HabitacionService;

import java.util.List;
import java.util.Map;

/** API (fetch) de habitaciones: listar con filtros y cambiar estado. */
@RestController
@RequestMapping("/recepcionista/api")
public class HabitacionApiController {

    private final HabitacionService habitacionService;

    public HabitacionApiController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping("/habitaciones")
    public List<HabitacionDTO> listar(@RequestParam(required = false) Integer piso,
                                      @RequestParam(required = false) Long idTipo,
                                      @RequestParam(required = false) Long idEstado) {
        return habitacionService.listar(piso, idTipo, idEstado);
    }

    @PostMapping("/habitaciones/{id}/estado")
    public Map<String, Object> cambiarEstado(@PathVariable Long id, @RequestParam Long idEstado) {
        habitacionService.cambiarEstado(id, idEstado);
        return Map.of("ok", true);
    }
}
