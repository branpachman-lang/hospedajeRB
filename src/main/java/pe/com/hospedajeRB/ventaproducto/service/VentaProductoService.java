package pe.com.hospedajeRB.ventaproducto.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.reservas.entity.Reserva;
import pe.com.hospedajeRB.ventaproducto.dto.*;
import pe.com.hospedajeRB.ventaproducto.entity.*;
import pe.com.hospedajeRB.ventaproducto.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaProductoService {

    private static final String SERIE_BOLETA = "B001";

    private final CategoriaVentaRepository categoriaRepo;
    private final ProductoVentaRepository productoRepo;
    private final ReservaVentaRepository reservaRepo;
    private final TurnoCajaVentaRepository turnoRepo;
    private final EstadoSesionVentaRepository estadoSesionRepo;
    private final TipoComprobanteVentaRepository tipoComprobanteRepo;
    private final EstadoComprobanteVentaRepository estadoComprobanteRepo;
    private final EstadoCargoVentaRepository estadoCargoRepo;
    private final FormaPagoVentaRepository formaPagoRepo;
    private final ComprobanteVentaRepository comprobanteRepo;
    private final PagoVentaRepository pagoRepo;
    private final CargoProductoVentaRepository cargoRepo;

    public VentaProductoService(CategoriaVentaRepository categoriaRepo,
                                ProductoVentaRepository productoRepo,
                                ReservaVentaRepository reservaRepo,
                                TurnoCajaVentaRepository turnoRepo,
                                EstadoSesionVentaRepository estadoSesionRepo,
                                TipoComprobanteVentaRepository tipoComprobanteRepo,
                                EstadoComprobanteVentaRepository estadoComprobanteRepo,
                                EstadoCargoVentaRepository estadoCargoRepo,
                                FormaPagoVentaRepository formaPagoRepo,
                                ComprobanteVentaRepository comprobanteRepo,
                                PagoVentaRepository pagoRepo,
                                CargoProductoVentaRepository cargoRepo) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
        this.reservaRepo = reservaRepo;
        this.turnoRepo = turnoRepo;
        this.estadoSesionRepo = estadoSesionRepo;
        this.tipoComprobanteRepo = tipoComprobanteRepo;
        this.estadoComprobanteRepo = estadoComprobanteRepo;
        this.estadoCargoRepo = estadoCargoRepo;
        this.formaPagoRepo = formaPagoRepo;
        this.comprobanteRepo = comprobanteRepo;
        this.pagoRepo = pagoRepo;
        this.cargoRepo = cargoRepo;
    }

    @Transactional(readOnly = true)
    public List<CategoriaVentaDTO> listarCategorias() {
        return categoriaRepo.findAllByOrderByNombreCategoriaAsc().stream()
                .map(c -> new CategoriaVentaDTO(c.getIdCategoria(), c.getNombreCategoria()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FormaPagoVentaDTO> listarFormasPago() {
        return formaPagoRepo.findAllByOrderByNombrePagoAsc().stream()
                .map(f -> new FormaPagoVentaDTO(f.getIdFormaPago(), f.getNombrePago()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVentaDTO> listarProductos(Long categoriaId, String q) {
        String filtro = (q == null || q.isBlank()) ? null : q.trim();
        return productoRepo.buscarCatalogo(categoriaId, filtro).stream().map(this::toProductoDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaVentaDTO> buscarReservas(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }
        return reservaRepo.buscarParaVenta(q.trim()).stream()
                .limit(8)
                .map(this::toReservaDTO)
                .toList();
    }

    @Transactional
    public ComprobanteVentaDTO confirmar(ConfirmarVentaRequestDTO request, Long idUsuario) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto al carrito.");
        }

        Reserva reserva = null;
        if (request.idReserva() != null) {
            reserva = reservaRepo.buscarDetalle(request.idReserva())
                    .orElseThrow(() -> new IllegalArgumentException("La reserva seleccionada no existe."));
        }
        if (request.cargarAHabitacion() && reserva == null) {
            throw new IllegalArgumentException("Seleccione una reserva para cargar la venta a habitacion.");
        }

        // Reutiliza el turno de caja ABIERTO del recepcionista; si no hay, lo abre automaticamente.
        TurnoCaja turno = turnoRepo.buscarAbiertosPorUsuario(idUsuario).stream()
                .findFirst()
                .orElseGet(() -> abrirTurnoAutomatico(idUsuario));

        TipoComprobante tipo = tipoComprobanteRepo.buscarPorNombre("Boleta")
                .orElseThrow(() -> new IllegalStateException("Falta el tipo de comprobante 'Boleta'."));
        EstadoComprobante estadoComprobante = estadoComprobanteRepo.buscarPorNombre("Emitido")
                .orElseThrow(() -> new IllegalStateException("Falta el estado de comprobante 'Emitido'."));
        EstadoCargo estadoCargo = resolverEstadoCargo(request.cargarAHabitacion());

        List<ItemProcesado> procesados = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemVentaRequestDTO item : request.items()) {
            Producto producto = productoRepo.bloquearPorId(item.idProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Un producto del carrito ya no existe."));
            int cantidad = item.cantidad() == null ? 0 : item.cantidad();
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            if (producto.getStock() == null || producto.getStock() < cantidad) {
                throw new IllegalStateException("No hay stock suficiente de " + producto.getNombreProducto()
                        + ". Disponible: " + (producto.getStock() == null ? 0 : producto.getStock()) + ".");
            }
            producto.setStock(producto.getStock() - cantidad);
            BigDecimal linea = producto.getPrecioActual().multiply(BigDecimal.valueOf(cantidad));
            total = total.add(linea);
            procesados.add(new ItemProcesado(producto, cantidad, producto.getPrecioActual(), linea));
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la venta debe ser mayor que cero.");
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setReserva(reserva);
        comprobante.setCliente(reserva == null ? null : reserva.getCliente());
        comprobante.setTipoComprobante(tipo);
        comprobante.setTurno(turno);
        comprobante.setSerie(SERIE_BOLETA);
        comprobante.setNumeroComprobante(siguienteNumero(SERIE_BOLETA));
        comprobante.setTotalPagar(total);
        comprobante.setFechaEmision(LocalDateTime.now());
        comprobante.setEstadoComprobante(estadoComprobante);
        comprobante = comprobanteRepo.save(comprobante);

        for (ItemProcesado item : procesados) {
            CargoProducto cargo = new CargoProducto();
            cargo.setReserva(reserva);
            cargo.setProducto(item.producto());
            cargo.setComprobante(comprobante);
            cargo.setCantidad(item.cantidad());
            cargo.setPrecioUnitario(item.precioUnitario());
            cargo.setEstadoCargo(estadoCargo);
            cargoRepo.save(cargo);
        }

        if (!request.cargarAHabitacion()) {
            FormaPago formaPago = request.idFormaPago() == null
                    ? formaPagoRepo.buscarPorNombre("Efectivo").orElseThrow(() -> new IllegalStateException("Falta la forma de pago 'Efectivo'."))
                    : formaPagoRepo.findById(request.idFormaPago()).orElseThrow(() -> new IllegalArgumentException("La forma de pago no existe."));
            Pago pago = new Pago();
            pago.setComprobante(comprobante);
            pago.setFormaPago(formaPago);
            pago.setIdTerceroUsuario(idUsuario);
            pago.setMonto(total);
            pago.setNumeroOperacion(request.numeroOperacion());
            pago.setVerificado(1);
            pago.setFechaPago(LocalDateTime.now());
            pagoRepo.save(pago);
        }

        BigDecimal subtotal = total.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);
        List<ItemComprobanteDTO> items = procesados.stream()
                .map(i -> new ItemComprobanteDTO(i.producto().getNombreProducto(), i.cantidad(), i.precioUnitario(), i.total()))
                .toList();

        return new ComprobanteVentaDTO(
                comprobante.getIdComprobante(),
                comprobante.getSerie(),
                comprobante.getNumeroComprobante(),
                comprobante.getFechaEmision(),
                subtotal,
                igv,
                total.setScale(2, RoundingMode.HALF_UP),
                request.cargarAHabitacion(),
                reserva == null ? null : toReservaDTO(reserva),
                items
        );
    }

    /** Abre un turno_caja para el usuario cuando no tiene uno abierto (respaldo mientras no exista el modulo de Caja). */
    private TurnoCaja abrirTurnoAutomatico(Long idUsuario) {
        EstadoSesion abierta = estadoSesionRepo.buscarPorNombre("Abierta")
                .or(() -> estadoSesionRepo.buscarPorNombre("Abierto"))
                .orElseThrow(() -> new IllegalStateException(
                        "Falta el estado de sesion 'Abierta' para abrir el turno de caja."));
        TurnoCaja t = new TurnoCaja();
        t.setIdTerceroUsuario(idUsuario);
        t.setFechaApertura(LocalDateTime.now());
        t.setMontoBase(BigDecimal.ZERO);
        t.setEstadoSesion(abierta);
        return turnoRepo.save(t);
    }

    private EstadoCargo resolverEstadoCargo(boolean cargarHabitacion) {
        if (cargarHabitacion) {
            return estadoCargoRepo.buscarPorNombre("Pendiente")
                    .orElseGet(() -> estadoCargoRepo.buscarPorNombre("Facturado")
                            .orElseThrow(() -> new IllegalStateException("Falta un estado de cargo 'Pendiente' o 'Facturado'.")));
        }
        return estadoCargoRepo.buscarPorNombre("Facturado")
                .orElseGet(() -> estadoCargoRepo.buscarPorNombre("Vendido")
                        .orElseThrow(() -> new IllegalStateException("Falta un estado de cargo 'Facturado' o 'Vendido'.")));
    }

    private String siguienteNumero(String serie) {
        String ultimo = comprobanteRepo.ultimoNumero(serie);
        long valor;
        try {
            valor = Long.parseLong(ultimo);
        } catch (NumberFormatException ignored) {
            valor = 0;
        }
        return String.format("%08d", valor + 1);
    }

    private ProductoVentaDTO toProductoDTO(Producto p) {
        int stock = p.getStock() == null ? 0 : p.getStock();
        int minimo = p.getStockMinimo() == null ? 0 : p.getStockMinimo();
        return new ProductoVentaDTO(
                p.getIdProducto(),
                p.getNombreProducto(),
                p.getCategoria() == null ? "Sin categoria" : p.getCategoria().getNombreCategoria(),
                p.getPrecioActual(),
                stock,
                minimo,
                stock > 0,
                stock <= minimo
        );
    }

    private ReservaVentaDTO toReservaDTO(Reserva r) {
        return new ReservaVentaDTO(
                r.getIdReserva(),
                r.getCodigoReserva(),
                r.getCliente() == null ? "" : r.getCliente().getRazonSocialNombre(),
                r.getHabitacion() == null ? "" : r.getHabitacion().getNumeroCuarto(),
                r.getHabitacion() == null || r.getHabitacion().getTipo() == null ? "" : r.getHabitacion().getTipo().getNombreTipo(),
                r.getHabitacion() == null ? "" : r.getHabitacion().getDescripcion(),
                r.getFechaIngreso(),
                r.getFechaSalida(),
                r.getCostoEstadia()
        );
    }

    private record ItemProcesado(Producto producto, int cantidad, BigDecimal precioUnitario, BigDecimal total) {
    }
}
