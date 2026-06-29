package pe.com.hospedajeRB.comprobantes.service;

import org.springframework.stereotype.Component;
import pe.com.hospedajeRB.ventaproducto.repository.ComprobanteVentaRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Numeracion de comprobantes: serie por tipo + numero DDMMYY + correlativo del dia. */
@Component
public class ComprobanteNumeracion {

    private static final DateTimeFormatter DDMMYY = DateTimeFormatter.ofPattern("ddMMyy");

    private final ComprobanteVentaRepository comprobanteRepo;

    public ComprobanteNumeracion(ComprobanteVentaRepository comprobanteRepo) {
        this.comprobanteRepo = comprobanteRepo;
    }

    /** Serie segun el tipo de comprobante. */
    public String serie(String tipoNombre) {
        if (tipoNombre == null) return "B001";
        String t = tipoNombre.toLowerCase();
        if (t.contains("factura")) return "F001";
        if (t.contains("nota")) return "NC01";
        return "B001";
    }

    /** Numero correlativo del dia para una serie: DDMMYY + 4 digitos. */
    public String siguienteNumero(String serie) {
        String pref = LocalDate.now().format(DDMMYY);
        long n = comprobanteRepo.contarDelDia(serie, pref + "%") + 1;
        return pref + String.format("%04d", n);
    }
}
