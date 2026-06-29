package pe.com.hospedajeRB.reservas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.hospedajeRB.habitaciones.entity.EstadoHabitacion;
import pe.com.hospedajeRB.habitaciones.entity.Habitacion;
import pe.com.hospedajeRB.habitaciones.repository.EstadoHabitacionRepository;
import pe.com.hospedajeRB.habitaciones.repository.HabitacionRepository;
import pe.com.hospedajeRB.reservas.dto.*;
import pe.com.hospedajeRB.reservas.entity.*;
import pe.com.hospedajeRB.reservas.repository.*;
import pe.com.hospedajeRB.seguridad.entity.Persona;
import pe.com.hospedajeRB.seguridad.entity.Tercero;
import pe.com.hospedajeRB.seguridad.repository.PersonaRepository;
import pe.com.hospedajeRB.seguridad.repository.TerceroRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** CRUD de reservas (modelo: una fila por habitacion, agrupadas por codigo). */
@Service
public class ReservaCrudService {

    private static final String ESTADO_INICIAL = "Check-in Pendiente"; // reserva hecha, sin pagar

    private final ReservaRepository reservaRepo;
    private final ReservaHuespedRepository huespedRepo;
    private final ClienteRepository clienteRepo;
    private final EstadoReservaRepository estadoReservaRepo;
    private final TipoOcupanteRepository tipoOcupanteRepo;
    private final HabitacionRepository habitacionRepo;
    private final EstadoHabitacionRepository estadoHabitacionRepo;
    private final TerceroRepository terceroRepo;
    private final PersonaRepository personaRepo;

    public ReservaCrudService(ReservaRepository reservaRepo, ReservaHuespedRepository huespedRepo,
                              ClienteRepository clienteRepo, EstadoReservaRepository estadoReservaRepo,
                              TipoOcupanteRepository tipoOcupanteRepo, HabitacionRepository habitacionRepo,
                              EstadoHabitacionRepository estadoHabitacionRepo, TerceroRepository terceroRepo,
                              PersonaRepository personaRepo) {
        this.reservaRepo = reservaRepo; this.huespedRepo = huespedRepo; this.clienteRepo = clienteRepo;
        this.estadoReservaRepo = estadoReservaRepo; this.tipoOcupanteRepo = tipoOcupanteRepo;
        this.habitacionRepo = habitacionRepo; this.estadoHabitacionRepo = estadoHabitacionRepo;
        this.terceroRepo = terceroRepo; this.personaRepo = personaRepo;
    }

    // ---------- LISTAR (agrupado por codigo) ----------
    @Transactional(readOnly = true)
    public List<ReservaListaDTO> listar(String filtroEstado) {
        List<Reserva> rows = (filtroEstado == null || filtroEstado.isBlank())
                ? reservaRepo.findAllByOrderByCodigoReservaDescIdReserva()
                : reservaRepo.findByEstadoReserva_NombreEstadoReservaOrderByCodigoReserva(filtroEstado);

        LinkedHashMap<String, List<Reserva>> grupos = new LinkedHashMap<>();
        for (Reserva r : rows) {
            String cod = r.getCodigoReserva() == null ? ("SIN-" + r.getIdReserva()) : r.getCodigoReserva();
            grupos.computeIfAbsent(cod, k -> new ArrayList<>()).add(r);
        }

        List<ReservaListaDTO> salida = new ArrayList<>();
        for (var e : grupos.entrySet()) {
            List<Reserva> g = e.getValue();
            Reserva primera = g.get(0);
            int huespedes = 0;
            for (Reserva r : g) huespedes += (int) huespedRepo.countByIdReserva(r.getIdReserva());
            String estado = primera.getEstadoReserva().getNombreEstadoReserva();
            boolean pagada = !estado.toLowerCase().contains("check-in pendiente");
            salida.add(new ReservaListaDTO(
                    e.getKey(), "N° " + e.getKey(),
                    primera.getCliente().getRazonSocialNombre(),
                    g.size(), huespedes, pagada, estado));
        }
        return salida;
    }

    // ---------- CREAR ----------
    @Transactional
    public String crear(CrearReservaDTO dto, Long idUsuario) {
        String codigo = nuevoCodigo();
        crearConCodigo(dto, idUsuario, codigo);
        return codigo;
    }

    private void crearConCodigo(CrearReservaDTO dto, Long idUsuario, String codigo) {
        if (dto.habitaciones() == null || dto.habitaciones().isEmpty())
            throw new IllegalArgumentException("Agrega al menos una habitación.");

        // No permitir documentos/telefonos repetidos dentro de la misma reserva
        List<PersonaDTO> todas = new ArrayList<>();
        todas.add(dto.cliente());
        for (HabitacionReservaDTO h : dto.habitaciones())
            if (h.huespedes() != null) todas.addAll(h.huespedes());
        Set<String> docs = new HashSet<>(), tels = new HashSet<>();
        for (PersonaDTO p : todas) {
            if (!isBlank(p.numeroDocumento()) && !docs.add(p.numeroDocumento().trim()))
                throw new IllegalArgumentException("Documento repetido en la reserva: " + p.numeroDocumento());
            if (!isBlank(p.telefono()) && !tels.add(p.telefono().trim()))
                throw new IllegalArgumentException("Teléfono repetido en la reserva: " + p.telefono());
        }

        // Validacion de fechas: check-out posterior al check-in
        List<HabitacionReservaDTO> hs = dto.habitaciones();
        for (HabitacionReservaDTO h : hs) {
            if (h.checkIn() == null || h.checkOut() == null)
                throw new IllegalArgumentException("Completa las fechas de Check-in y Check-out de cada habitación.");
            if (!h.checkOut().isAfter(h.checkIn()))
                throw new IllegalArgumentException("El Check-out debe ser posterior al Check-in.");
        }
        // Sin cruces entre habitaciones de la MISMA reserva (misma habitacion repetida)
        for (int i = 0; i < hs.size(); i++)
            for (int j = i + 1; j < hs.size(); j++) {
                HabitacionReservaDTO a = hs.get(i), b = hs.get(j);
                if (Objects.equals(a.idHabitacion(), b.idHabitacion())
                        && a.checkIn().isBefore(b.checkOut()) && b.checkIn().isBefore(a.checkOut()))
                    throw new IllegalArgumentException(
                            "Hay dos asignaciones de la misma habitación con fechas que se cruzan.");
            }

        // Validaciones de negocio del cliente
        validar(dto.cliente());

        // Cliente (tercero + persona + cliente)
        Long idCliente = guardarPersona(dto.cliente());
        Cliente cli = clienteRepo.findById(idCliente).orElseGet(Cliente::new);
        cli.setIdTercero(idCliente);
        cli.setTipoCliente("Natural");
        cli.setRazonSocialNombre((nz(dto.cliente().nombres()) + " " + nz(dto.cliente().apellidos())).trim());
        cli.setDireccionFiscal(dto.cliente().direccion());
        clienteRepo.save(cli);

        EstadoReserva estadoInicial = estadoReservaRepo.findFirstByNombreEstadoReservaIgnoreCase(ESTADO_INICIAL)
                .orElseThrow(() -> new IllegalStateException("Falta el estado de reserva '" + ESTADO_INICIAL + "'"));
        Long tipoOcupante = tipoOcupanteRepo.findAll().stream().findFirst()
                .map(TipoOcupante::getIdTipoOcupante).orElse(1L);
        EstadoHabitacion reservada = estadoHabitacionRepo.findFirstByNombreEstadoIgnoreCase("Reservada").orElse(null);

        for (HabitacionReservaDTO h : dto.habitaciones()) {
            Habitacion hab = habitacionRepo.findById(h.idHabitacion())
                    .orElseThrow(() -> new IllegalArgumentException("Habitación no existe"));
            int cap = hab.getTipo().getCapacidadMax();
            int nh = h.huespedes() == null ? 0 : h.huespedes().size();
            if (nh > cap)
                throw new IllegalArgumentException("La habitación " + hab.getNumeroCuarto() +
                        " admite máximo " + cap + " huéspedes (intentaste " + nh + ").");

            // Sin cruce con reservas ya existentes de la misma habitacion (excluye la propia, por codigo)
            long solapadas = reservaRepo.contarSolapadas(hab.getIdHabitacion(), h.checkIn(), h.checkOut(), codigo);
            if (solapadas > 0)
                throw new IllegalArgumentException("La habitación " + hab.getNumeroCuarto() +
                        " ya tiene una reserva que se cruza con esas fechas.");

            long noches = Math.max(1, ChronoUnit.DAYS.between(h.checkIn(), h.checkOut()));
            BigDecimal costo = hab.getTipo().getPrecioBase().multiply(BigDecimal.valueOf(noches));

            Reserva r = new Reserva();
            r.setCliente(cli);
            r.setHabitacion(hab);
            r.setCostoEstadia(costo);
            r.setFechaIngreso(h.checkIn());
            r.setFechaSalida(h.checkOut());
            r.setEstadoReserva(estadoInicial);
            r.setIdTerceroUsuario(idUsuario);
            r.setCodigoReserva(codigo);
            r = reservaRepo.save(r);

            if (reservada != null) { hab.setEstado(reservada); habitacionRepo.save(hab); }

            if (h.huespedes() != null) {
                for (PersonaDTO hu : h.huespedes()) {
                    validar(hu);
                    Long idH = guardarPersona(hu);
                    ReservaHuesped rh = new ReservaHuesped();
                    rh.setIdReserva(r.getIdReserva());
                    rh.setIdTercero(idH);
                    rh.setIdTipoOcupante(tipoOcupante);
                    huespedRepo.save(rh);
                }
            }
        }
    }

    // ---------- DETALLE (para editar) ----------
    @Transactional(readOnly = true)
    public ReservaDetalleDTO detalle(String codigo) {
        List<Reserva> g = reservaRepo.findByCodigoReservaOrderByIdReserva(codigo);
        if (g.isEmpty()) throw new IllegalArgumentException("Reserva no encontrada");
        Reserva primera = g.get(0);
        PersonaDTO cliente = personaDTOcliente(primera.getCliente().getIdTercero());

        List<HabitacionReservaDTO> habs = new ArrayList<>();
        for (Reserva r : g) {
            List<PersonaDTO> hus = new ArrayList<>();
            for (ReservaHuesped rh : huespedRepo.findByIdReserva(r.getIdReserva()))
                hus.add(personaDTO(rh.getIdTercero(), null));
            habs.add(new HabitacionReservaDTO(r.getHabitacion().getIdHabitacion(),
                    r.getFechaIngreso(), r.getFechaSalida(), hus));
        }
        return new ReservaDetalleDTO(codigo, cliente, habs);
    }

    // ---------- EDITAR (reemplaza el grupo manteniendo el codigo) ----------
    @Transactional
    public void editar(String codigo, CrearReservaDTO dto, Long idUsuario) {
        borrarGrupo(codigo, false);
        crearConCodigo(dto, idUsuario, codigo);
    }

    // ---------- ELIMINAR (solo si no esta pagada) ----------
    @Transactional
    public void eliminar(String codigo) {
        borrarGrupo(codigo, true);
    }

    private void borrarGrupo(String codigo, boolean validarPago) {
        List<Reserva> g = reservaRepo.findByCodigoReservaOrderByIdReserva(codigo);
        if (g.isEmpty()) return;
        if (validarPago) {
            boolean pagada = g.stream().anyMatch(r ->
                    !r.getEstadoReserva().getNombreEstadoReserva().toLowerCase().contains("check-in pendiente"));
            if (pagada) throw new IllegalArgumentException(
                    "No se puede eliminar: la reserva ya fue confirmada/pagada. Usa una nota de crédito.");
        }
        EstadoHabitacion disponible = estadoHabitacionRepo.findFirstByNombreEstadoIgnoreCase("Disponible").orElse(null);
        for (Reserva r : g) {
            huespedRepo.deleteByIdReserva(r.getIdReserva());
            if (disponible != null) { Habitacion hab = r.getHabitacion(); hab.setEstado(disponible); habitacionRepo.save(hab); }
            reservaRepo.delete(r);
        }
        reservaRepo.flush(); // asegura que el grupo borrado no cuente en la validacion de solapamiento al re-crear
    }

    // ---------- BUSCAR cliente/huesped por documento (RF06) ----------
    @Transactional(readOnly = true)
    public PersonaDTO buscarPorDocumento(String doc) {
        var t = terceroRepo.findFirstByNumeroDocumento(doc);
        if (t.isEmpty()) return null;
        return personaDTOcliente(t.get().getIdTercero());
    }

    // ---------- COMPROBANTE (stub: requiere modulo de Caja) ----------
    @Transactional(readOnly = true)
    public String comprobante(String codigo) {
        // RF11: emitir comprobante necesita turno de caja, tipo de comprobante y pago.
        // Se completara con el modulo de Caja. Por ahora informamos.
        return "La emisión de comprobante estará disponible al implementar el módulo de Caja " +
               "(turno de caja + pago). Reserva " + codigo + " lista para facturar.";
    }

    // ---------- helpers ----------
    private String nuevoCodigo() {
        String prefijo = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long n = reservaRepo.contarCodigosDelDia(prefijo + "%") + 1;
        return prefijo + String.format("%06d", n);
    }

    /** Crea o reutiliza tercero+persona por numero de documento. Devuelve id_tercero. */
    private Long guardarPersona(PersonaDTO p) {
        Tercero t = (p.numeroDocumento() == null ? Optional.<Tercero>empty()
                : terceroRepo.findFirstByNumeroDocumento(p.numeroDocumento())).orElseGet(Tercero::new);
        t.setIdTipoDocumento(p.idTipoDocumento() != null ? p.idTipoDocumento() : 1L);
        t.setNumeroDocumento(p.numeroDocumento());
        t.setCorreo(p.correo());
        t.setTelefono(p.telefono());
        t = terceroRepo.save(t);

        Persona per = personaRepo.findById(t.getIdTercero()).orElseGet(Persona::new);
        per.setIdTercero(t.getIdTercero());
        per.setNombres(nz(p.nombres()));
        per.setApellidos(nz(p.apellidos()));
        per.setFechaNacimiento(p.fechaNacimiento());
        personaRepo.save(per);
        return t.getIdTercero();
    }

    private PersonaDTO personaDTO(Long idTercero, String direccion) {
        Tercero t = terceroRepo.findById(idTercero).orElse(null);
        Persona p = personaRepo.findById(idTercero).orElse(null);
        return new PersonaDTO(
                p != null ? p.getNombres() : null,
                p != null ? p.getApellidos() : null,
                p != null ? p.getFechaNacimiento() : null,
                direccion,
                t != null ? t.getIdTipoDocumento() : 1L,
                t != null ? t.getNumeroDocumento() : null,
                t != null ? t.getCorreo() : null,
                t != null ? t.getTelefono() : null);
    }

    private PersonaDTO personaDTOcliente(Long idTercero) {
        Cliente c = clienteRepo.findById(idTercero).orElse(null);
        return personaDTO(idTercero, c != null ? c.getDireccionFiscal() : null);
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** Validaciones de negocio para cliente y huespedes (mayoria de edad, DNI, correo, telefono). */
    private void validar(PersonaDTO p) {
        if (isBlank(p.nombres()) || isBlank(p.apellidos()))
            throw new IllegalArgumentException("Nombres y apellidos son obligatorios.");
        if (p.fechaNacimiento() == null ||
            ChronoUnit.YEARS.between(p.fechaNacimiento(), LocalDate.now()) < 18)
            throw new IllegalArgumentException("Cada persona debe ser mayor de edad (18 años o más).");
        if (isBlank(p.numeroDocumento()))
            throw new IllegalArgumentException("El N° de documento es obligatorio.");
        if (p.idTipoDocumento() != null && p.idTipoDocumento() == 1L
                && !p.numeroDocumento().matches("\\d{8}"))
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
        if (!isBlank(p.correo()) && !p.correo().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Correo inválido: " + p.correo());
        if (!isBlank(p.telefono()) && !p.telefono().matches("\\d{9}"))
            throw new IllegalArgumentException("Teléfono inválido (debe tener 9 dígitos): " + p.telefono());
    }
}
