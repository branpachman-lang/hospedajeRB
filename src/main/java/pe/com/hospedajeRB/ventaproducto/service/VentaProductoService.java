package pe.com.hospedajeRB.ventaproducto.service;

import java.util.List;
import pe.com.hospedajeRB.ventaproducto.dto.*;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface VentaProductoService {

    List<CategoriaVentaDTO> listarCategorias();

    List<FormaPagoVentaDTO> listarFormasPago();

    List<ProductoVentaDTO> listarProductos(Long categoriaId, String q);

    List<ReservaVentaDTO> buscarReservas(String q);

    ComprobanteVentaDTO confirmar(ConfirmarVentaRequestDTO request, Long idUsuario);

}
