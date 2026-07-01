package pe.com.hospedajeRB.comprobantes.service;

import java.util.List;
import pe.com.hospedajeRB.comprobantes.dto.*;
import pe.com.hospedajeRB.ventaproducto.dto.FormaPagoVentaDTO;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface ComprobanteService {

    List<FormaPagoVentaDTO> listarFormasPago();

    List<String> listarTiposComprobante();

    ResumenPagoDTO resumen(String codigo);

    ComprobanteResultDTO emitir(String codigo, EmitirComprobanteRequestDTO req, Long idUsuario);

    ComprobanteResultDTO anular(String codigo, Long idUsuario);

    Long comprobanteVigente(String codigo);

    TicketDTO ticket(Long idComprobante);

}
