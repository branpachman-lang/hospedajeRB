package pe.com.hospedajeRB.comprobantes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.comprobantes.dto.*;
import pe.com.hospedajeRB.habitaciones.entity.EstadoHabitacion;
import pe.com.hospedajeRB.habitaciones.entity.Habitacion;
import pe.com.hospedajeRB.habitaciones.repository.EstadoHabitacionRepository;
import pe.com.hospedajeRB.habitaciones.repository.HabitacionRepository;
import pe.com.hospedajeRB.reservas.entity.Cliente;
import pe.com.hospedajeRB.reservas.entity.EstadoReserva;
import pe.com.hospedajeRB.reservas.entity.Reserva;
import pe.com.hospedajeRB.reservas.repository.EstadoReservaRepository;
import pe.com.hospedajeRB.reservas.repository.ReservaHuespedRepository;
import pe.com.hospedajeRB.reservas.repository.ReservaRepository;
import pe.com.hospedajeRB.seguridad.entity.Persona;
import pe.com.hospedajeRB.seguridad.entity.Tercero;
import pe.com.hospedajeRB.seguridad.repository.PersonaRepository;
import pe.com.hospedajeRB.seguridad.repository.TerceroRepository;
import pe.com.hospedajeRB.reservas.repository.TipoDocumentoRepository;
import pe.com.hospedajeRB.ventaproducto.dto.FormaPagoVentaDTO;
import pe.com.hospedajeRB.ventaproducto.entity.*;
import pe.com.hospedajeRB.ventaproducto.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Pago de reservas: emision de comprobante (boleta/factura), nota de credito y ticket. */
@Service
public class ComprobanteService {

    private static final String EMPRESA = "HOSPEDAJE REAL BOLOGNESI";
    private static final String RUC_EMPRESA = "20000000000";          // TODO: reemplazar por el RUC real
    private static final String DIR_EMPRESA = "Jr. Bolognesi - Huaraz";

    private final ReservaRepository reservaRepo;
    private final ReservaHuespedRepository huespedRepo;
    private final EstadoReservaRepository estadoReservaRepo;
    private final TerceroRepository terceroRepo;
    private final TipoDocumentoRepository tipoDocRepo;
    private final PersonaRepository personaRepo;
    private final HabitacionRepository habitacionRepo;
    private final EstadoHabitacionRepository estadoHabRepo;
    private final ComprobanteNumeracion numeracion;

    private final ComprobanteVentaRepository comprobanteRepo;
    private final PagoVentaRepository pagoRepo;
    private final FormaPagoVentaRepository formaPagoRepo;
    private final TipoComprobanteVentaRepository tipoComprobanteRepo;
    private final EstadoComprobanteVentaRepository estadoComprobanteRepo;
    private final TurnoCajaVentaRepository turnoRepo;
    private final EstadoSesionVentaRepository estadoSesionRepo;
    private final CargoProductoVentaRepository cargoRepo;
    private final EstadoCargoVentaRepository estadoCargoRepo;
    private final ProductoVentaRepository productoRepo;

    public ComprobanteService(ReservaRepository reservaRepo, ReservaHuespedRepository huespedRepo,
                              EstadoReservaRepository estadoReservaRepo, TerceroRepository terceroRepo,
                              TipoDocumentoRepository tipoDocRepo, PersonaRepository personaRepo,
                              HabitacionRepository habitacionRepo, EstadoHabitacionRepository estadoHabRepo,
                              ComprobanteNumeracion numeracion, ComprobanteVentaRepository comprobanteRepo,
                              PagoVentaRepository pagoRepo, FormaPagoVentaRepository formaPagoRepo,
                              TipoComprobanteVentaRepository tipoComprobanteRepo,
                              EstadoComprobanteVentaRepository estadoComprobanteRepo,
                              TurnoCajaVentaRepository turnoRepo, EstadoSesionVentaRepository estadoSesionRepo,
                              CargoProductoVentaRepository cargoRepo, EstadoCargoVentaRepository estadoCargoRepo,
                              ProductoVentaRepository productoRepo) {
        this.reservaRepo = reservaRepo; this.huespedRepo = huespedRepo; this.estadoReservaRepo = estadoReservaRepo;
        this.terceroRepo = terceroRepo; this.tipoDocRepo = tipoDocRepo; this.personaRepo = personaRepo;
        this.habitacionRepo = habitacionRepo; this.estadoHabRepo = estadoHabRepo; this.numeracion = numeracion;
        this.comprobanteRepo = comprobanteRepo; this.pagoRepo = pagoRepo; this.formaPagoRepo = formaPagoRepo;
        this.tipoComprobanteRepo = tipoComprobanteRepo; this.estadoComprobanteRepo = estadoComprobanteRepo;
        this.turnoRepo = turnoRepo; this.estadoSesionRepo = estadoSesionRepo; this.cargoRepo = cargoRepo;
        this.estadoCargoRepo = estadoCargoRepo; this.productoRepo = productoRepo;
    }

    // ---------- catalogos para el modal ----------
    @Transactional(readOnly = true)
    public List<FormaPagoVentaDTO> listarFormasPago() {
        return formaPagoRepo.findAllByOrderByNombrePagoAsc().stream()
                .map(f -> new FormaPagoVentaDTO(f.getIdFormaPago(), f.getNombrePago()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarTiposComprobante() {
        return tipoComprobanteRepo.findAll().stream()
                .map(TipoComprobante::getNombreComprobante)
                .filter(n -> { String t = n.toLowerCase(); return t.contains("boleta") || t.contains("factura"); })
                .toList();
    }

    // ---------- resumen para el modal ----------
    @Transactional(readOnly = true)
    public ResumenPagoDTO resumen(String codigo) {
        List<Reserva> g = reservaRepo.findByCodigoReservaOrderByIdReserva(codigo);
        if (g.isEmpty()) throw new IllegalArgumentException("Reserva no encontrada.");
        Reserva primera = g.get(0);
        Cliente cli = primera.getCliente();
        Tercero t = terceroRepo.findById(cli.getIdTercero()).orElse(null);
        String doc = t != null ? t.getNumeroDocumento() : "";
        String tipoDoc = (t != null) ? tipoDocRepo.findById(t.getIdTipoDocumento())
                .map(td -> td.getNombreDocumento()).orElse("") : "";
        boolean esRuc = doc != null && doc.matches("(10|20)\\d{9}");

        List<LineaResumenDTO> habs = new ArrayList<>();
        int huespedes = 0;
        BigDecimal totalEstadia = BigDecimal.ZERO;
        List<Long> ids = new ArrayList<>();
        for (Reserva r : g) {
            ids.add(r.getIdReserva());
            long noches = Math.max(1, ChronoUnit.DAYS.between(r.getFechaIngreso(), r.getFechaSalida()));
            Habitacion h = r.getHabitacion();
            String tipoHab = h.getTipo() != null ? h.getTipo().getNombreTipo() : "";
            habs.add(new LineaResumenDTO(
                    "Hab. " + h.getNumeroCuarto() + " (" + tipoHab + ")",
                    r.getFechaIngreso() + " → " + r.getFechaSalida() + " · " + noches + " noche(s)",
                    (int) noches, r.getCostoEstadia()));
            totalEstadia = totalEstadia.add(r.getCostoEstadia());
            huespedes += (int) huespedRepo.countByIdReserva(r.getIdReserva());
        }

        List<LineaResumenDTO> cargos = new ArrayList<>();
        BigDecimal totalCargos = BigDecimal.ZERO;
        for (CargoProducto cp : cargoRepo.pendientesPorReservas(ids)) {
            BigDecimal imp = cp.getPrecioUnitario().multiply(BigDecimal.valueOf(cp.getCantidad()));
            cargos.add(new LineaResumenDTO(cp.getProducto().getNombreProducto(),
                    "x" + cp.getCantidad(), cp.getCantidad(), imp));
            totalCargos = totalCargos.add(imp);
        }

        boolean yaPagada = g.stream().anyMatch(r ->
                !r.getEstadoReserva().getNombreEstadoReserva().toLowerCase().contains("check-in pendiente"));

        return new ResumenPagoDTO(codigo, cli.getRazonSocialNombre(), doc, tipoDoc, esRuc,
                habs, huespedes, cargos, totalEstadia, totalCargos,
                totalEstadia.add(totalCargos), yaPagada);
    }

    // ---------- emitir comprobante (pago de la reserva) ----------
    @Transactional
    public ComprobanteResultDTO emitir(String codigo, EmitirComprobanteRequestDTO req, Long idUsuario) {
        List<Reserva> g = reservaRepo.findByCodigoReservaOrderByIdReserva(codigo);
        if (g.isEmpty()) throw new IllegalArgumentException("Reserva no encontrada.");
        boolean yaPagada = g.stream().anyMatch(r ->
                !r.getEstadoReserva().getNombreEstadoReserva().toLowerCase().contains("check-in pendiente"));
        if (yaPagada) throw new IllegalStateException("La reserva ya tiene un comprobante emitido.");

        Reserva primera = g.get(0);
        Cliente cli = primera.getCliente();
        Tercero t = terceroRepo.findById(cli.getIdTercero()).orElse(null);
        String doc = t != null ? t.getNumeroDocumento() : "";

        String tipoNombre = (req.tipoComprobante() == null || req.tipoComprobante().isBlank())
                ? "Boleta" : req.tipoComprobante().trim();
        if (tipoNombre.toLowerCase().contains("factura") && !(doc != null && doc.matches("(10|20)\\d{9}"))) {
            throw new IllegalArgumentException("Para emitir Factura el cliente debe tener RUC (11 dígitos que inicien en 10 o 20).");
        }

        TipoComprobante tipo = tipoComprobanteRepo.buscarPorNombre(tipoNombre)
                .orElseThrow(() -> new IllegalStateException("Falta el tipo de comprobante '" + tipoNombre + "'."));
        EstadoComprobante emitido = estadoComprobanteRepo.buscarPorNombre("Emitido")
                .orElseThrow(() -> new IllegalStateException("Falta el estado de comprobante 'Emitido'."));
        TurnoCaja turno = turnoAbierto(idUsuario);

        // Totales: estadia + cargos pendientes a habitacion
        List<Long> ids = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Reserva r : g) { ids.add(r.getIdReserva()); total = total.add(r.getCostoEstadia()); }
        List<CargoProducto> cargos = cargoRepo.pendientesPorReservas(ids);
        for (CargoProducto cp : cargos) total = total.add(cp.getPrecioUnitario().multiply(BigDecimal.valueOf(cp.getCantidad())));

        String serie = numeracion.serie(tipoNombre);
        Comprobante c = new Comprobante();
        c.setReserva(primera);
        c.setCliente(cli);
        c.setTipoComprobante(tipo);
        c.setTurno(turno);
        c.setSerie(serie);
        c.setNumeroComprobante(numeracion.siguienteNumero(serie));
        c.setTotalPagar(total);
        c.setFechaEmision(LocalDateTime.now());
        c.setEstadoComprobante(emitido);
        c = comprobanteRepo.save(c);

        // Pago
        FormaPago forma = req.idFormaPago() == null
                ? formaPagoRepo.buscarPorNombre("Efectivo").orElseThrow(() -> new IllegalStateException("Falta la forma de pago 'Efectivo'."))
                : formaPagoRepo.findById(req.idFormaPago()).orElseThrow(() -> new IllegalArgumentException("La forma de pago no existe."));
        Pago pago = new Pago();
        pago.setComprobante(c);
        pago.setFormaPago(forma);
        pago.setIdTerceroUsuario(idUsuario);
        pago.setMonto(total);
        pago.setVerificado(1);
        pago.setFechaPago(LocalDateTime.now());
        pagoRepo.save(pago);

        // Facturar cargos pendientes
        EstadoCargo facturado = estadoCargoRepo.buscarPorNombre("Facturado").orElse(null);
        for (CargoProducto cp : cargos) {
            cp.setComprobante(c);
            if (facturado != null) cp.setEstadoCargo(facturado);
            cargoRepo.save(cp);
        }

        // Reserva -> Check-out Pendiente (pagada, aun no sale)
        EstadoReserva checkout = estadoReservaRepo.findFirstByNombreEstadoReservaIgnoreCase("Check-out Pendiente")
                .orElseThrow(() -> new IllegalStateException("Falta el estado de reserva 'Check-out Pendiente'."));
        for (Reserva r : g) { r.setEstadoReserva(checkout); reservaRepo.save(r); }

        return new ComprobanteResultDTO(c.getIdComprobante(), c.getSerie(), c.getNumeroComprobante(), tipoNombre, total);
    }

    // ---------- anular reserva pagada -> NOTA DE CREDITO ----------
    @Transactional
    public ComprobanteResultDTO anular(String codigo, Long idUsuario) {
        List<Reserva> g = reservaRepo.findByCodigoReservaOrderByIdReserva(codigo);
        if (g.isEmpty()) throw new IllegalArgumentException("Reserva no encontrada.");
        List<Long> ids = new ArrayList<>();
        for (Reserva r : g) ids.add(r.getIdReserva());

        List<Comprobante> vigentes = comprobanteRepo.vigentesPorReservas(ids);
        if (vigentes.isEmpty())
            throw new IllegalStateException("La reserva no tiene comprobante emitido. Si no está pagada, usa Eliminar.");
        Comprobante original = vigentes.get(0);

        TipoComprobante tipoNC = tipoComprobanteRepo.buscarPorNombre("Nota de Crédito")
                .or(() -> tipoComprobanteRepo.buscarPorNombre("Nota de Credito"))
                .orElseThrow(() -> new IllegalStateException("Falta el tipo de comprobante 'Nota de Crédito'."));
        EstadoComprobante emitido = estadoComprobanteRepo.buscarPorNombre("Emitido")
                .orElseThrow(() -> new IllegalStateException("Falta el estado 'Emitido'."));
        EstadoComprobante anuladoEst = estadoComprobanteRepo.buscarPorNombre("Anulado")
                .orElseThrow(() -> new IllegalStateException("Falta el estado de comprobante 'Anulado'."));
        TurnoCaja turno = turnoAbierto(idUsuario);

        String serieNC = numeracion.serie("Nota de Crédito");
        Comprobante nc = new Comprobante();
        nc.setReserva(original.getReserva());
        nc.setCliente(original.getCliente());
        nc.setTipoComprobante(tipoNC);
        nc.setTurno(turno);
        nc.setSerie(serieNC);
        nc.setNumeroComprobante(numeracion.siguienteNumero(serieNC));
        nc.setTotalPagar(original.getTotalPagar());
        nc.setFechaEmision(LocalDateTime.now());
        nc.setEstadoComprobante(emitido);
        nc.setComprobanteOrigen(original);
        nc = comprobanteRepo.save(nc);

        // El comprobante original queda anulado
        original.setEstadoComprobante(anuladoEst);
        comprobanteRepo.save(original);

        // Revertir cargos del comprobante original: reponer stock y marcarlos anulados
        EstadoCargo cargoAnulado = estadoCargoRepo.buscarPorNombre("Anulado").orElse(null);
        for (CargoProducto cp : cargoRepo.porComprobante(original.getIdComprobante())) {
            Producto p = cp.getProducto();
            if (p.getStock() != null) { p.setStock(p.getStock() + cp.getCantidad()); productoRepo.save(p); }
            if (cargoAnulado != null) cp.setEstadoCargo(cargoAnulado);
            cargoRepo.save(cp);
        }

        // Reserva -> Cancelada, habitaciones -> Disponible
        EstadoReserva cancelada = estadoReservaRepo.findFirstByNombreEstadoReservaIgnoreCase("Cancelada")
                .orElseThrow(() -> new IllegalStateException("Falta el estado de reserva 'Cancelada'."));
        EstadoHabitacion disponible = estadoHabRepo.findFirstByNombreEstadoIgnoreCase("Disponible").orElse(null);
        for (Reserva r : g) {
            r.setEstadoReserva(cancelada);
            if (disponible != null) { Habitacion h = r.getHabitacion(); h.setEstado(disponible); habitacionRepo.save(h); }
            reservaRepo.save(r);
        }

        return new ComprobanteResultDTO(nc.getIdComprobante(), nc.getSerie(), nc.getNumeroComprobante(),
                "Nota de Crédito", original.getTotalPagar());
    }

    // ---------- id del comprobante vigente (para reimprimir) ----------
    @Transactional(readOnly = true)
    public Long comprobanteVigente(String codigo) {
        List<Reserva> g = reservaRepo.findByCodigoReservaOrderByIdReserva(codigo);
        if (g.isEmpty()) return null;
        List<Long> ids = new ArrayList<>();
        for (Reserva r : g) ids.add(r.getIdReserva());
        List<Comprobante> v = comprobanteRepo.vigentesPorReservas(ids);
        return v.isEmpty() ? null : v.get(0).getIdComprobante();
    }

    // ---------- ticket imprimible ----------
    @Transactional(readOnly = true)
    public TicketDTO ticket(Long idComprobante) {
        Comprobante c = comprobanteRepo.findById(idComprobante)
                .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado."));

        List<LineaTicketDTO> estadia = new ArrayList<>();
        if (c.getReserva() != null && c.getReserva().getCodigoReserva() != null) {
            for (Reserva r : reservaRepo.findByCodigoReservaOrderByIdReserva(c.getReserva().getCodigoReserva())) {
                long noches = Math.max(1, ChronoUnit.DAYS.between(r.getFechaIngreso(), r.getFechaSalida()));
                Habitacion h = r.getHabitacion();
                BigDecimal porNoche = r.getCostoEstadia().divide(BigDecimal.valueOf(noches), 2, RoundingMode.HALF_UP);
                estadia.add(new LineaTicketDTO("Hab " + h.getNumeroCuarto() + " x" + noches + "n",
                        (int) noches, porNoche, r.getCostoEstadia()));
            }
        }
        List<LineaTicketDTO> productos = new ArrayList<>();
        for (CargoProducto cp : cargoRepo.porComprobante(c.getIdComprobante())) {
            productos.add(new LineaTicketDTO(cp.getProducto().getNombreProducto(), cp.getCantidad(),
                    cp.getPrecioUnitario(), cp.getPrecioUnitario().multiply(BigDecimal.valueOf(cp.getCantidad()))));
        }

        BigDecimal total = c.getTotalPagar();
        BigDecimal subtotal = total.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        Pago pago = pagoRepo.findFirstByComprobante_IdComprobante(c.getIdComprobante());
        String formaPago = pago != null && pago.getFormaPago() != null ? pago.getFormaPago().getNombrePago() : "-";

        String cliente = c.getCliente() != null ? c.getCliente().getRazonSocialNombre() : "Cliente varios";
        String docTipo = "", docNumero = "";
        if (c.getCliente() != null) {
            Tercero t = terceroRepo.findById(c.getCliente().getIdTercero()).orElse(null);
            if (t != null) {
                docNumero = t.getNumeroDocumento();
                docTipo = tipoDocRepo.findById(t.getIdTipoDocumento()).map(td -> td.getNombreDocumento()).orElse("");
            }
        }

        String cajero = "";
        if (c.getTurno() != null) {
            Persona per = personaRepo.findById(c.getTurno().getIdTerceroUsuario()).orElse(null);
            if (per != null) cajero = (per.getNombres() + " " + per.getApellidos()).trim();
        }

        boolean anulado = c.getEstadoComprobante() != null
                && c.getEstadoComprobante().getNombreEstado().equalsIgnoreCase("Anulado");

        return new TicketDTO(EMPRESA, RUC_EMPRESA, DIR_EMPRESA,
                c.getTipoComprobante().getNombreComprobante(), c.getSerie(), c.getNumeroComprobante(),
                c.getFechaEmision(), cliente, docTipo, docNumero,
                estadia, productos, subtotal, igv, total, formaPago, cajero, anulado);
    }

    // ---------- helper: turno abierto o auto-apertura ----------
    private TurnoCaja turnoAbierto(Long idUsuario) {
        return turnoRepo.buscarAbiertosPorUsuario(idUsuario).stream().findFirst()
                .orElseGet(() -> {
                    EstadoSesion abierta = estadoSesionRepo.buscarPorNombre("Abierta")
                            .or(() -> estadoSesionRepo.buscarPorNombre("Abierto"))
                            .orElseThrow(() -> new IllegalStateException("Falta el estado de sesión 'Abierta'."));
                    TurnoCaja tc = new TurnoCaja();
                    tc.setIdTerceroUsuario(idUsuario);
                    tc.setFechaApertura(LocalDateTime.now());
                    tc.setMontoBase(BigDecimal.ZERO);
                    tc.setEstadoSesion(abierta);
                    return turnoRepo.save(tc);
                });
    }
}
