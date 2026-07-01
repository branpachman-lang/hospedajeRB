package pe.com.hospedajeRB.reservas.service;

import java.util.List;
import pe.com.hospedajeRB.reservas.dto.*;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface ReservaCrudService {

    List<ReservaListaDTO> listar(String filtroEstado);

    String crear(CrearReservaDTO dto, Long idUsuario);

    ReservaDetalleDTO detalle(String codigo);

    void editar(String codigo, CrearReservaDTO dto, Long idUsuario);

    void eliminar(String codigo);

    PersonaDTO buscarPorDocumento(String doc);

    String comprobante(String codigo);

}
