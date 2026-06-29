package pe.com.hospedajeRB.seguridad.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.com.hospedajeRB.seguridad.entity.Usuario;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador entre nuestra entidad Usuario y el UserDetails de Spring Security.
 * Aqui se traducen las reglas del Excel a los flags que Spring entiende:
 *
 *  - isEnabled()            -> estado Activo/Inactivo (RF47 / RF48)
 *  - isAccountNonLocked()   -> bloqueo por intentos fallidos (RN08 / RNF04)
 *  - getAuthorities()       -> ROLE_<nombreRol> para el RBAC (RF40 / RNF06 / RN12)
 *  - esTemporal()           -> fuerza cambio de password (RF36 / RF37 / RF39)
 */
public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;
    private final boolean cuentaNoBloqueada;

    public UsuarioPrincipal(Usuario usuario, boolean cuentaNoBloqueada) {
        this.usuario = usuario;
        this.cuentaNoBloqueada = cuentaNoBloqueada;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + normaliza(r.getNombreRol())))
                .collect(Collectors.toSet());
    }

    private String normaliza(String nombre) {
        return nombre == null ? "" : nombre.trim().toUpperCase().replace(' ', '_');
    }

    @Override public String getPassword() { return usuario.getPassword(); }
    @Override public String getUsername() { return usuario.getUsername(); }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** RN08 / RNF04: cuenta bloqueada temporalmente por intentos fallidos. */
    @Override public boolean isAccountNonLocked() { return cuentaNoBloqueada; }

    /** RF47 / RF48: solo los usuarios Activos pueden iniciar sesion. */
    @Override
    public boolean isEnabled() {
        return usuario.getEstadoUsuario() != null && usuario.getEstadoUsuario().esActivo();
    }

    // ---- helpers de negocio ----
    public boolean esTemporal()      { return usuario.esTemporal(); }
    public Long getIdTercero()       { return usuario.getIdTercero(); }
    public LocalDateTime getFechaBloqueo() { return usuario.getFechaBloqueo(); }

    public String getNombreParaMostrar() {
        if (usuario.getPersona() != null && usuario.getPersona().getNombres() != null) {
            return usuario.getPersona().nombreCompleto().trim();
        }
        return usuario.getUsername();
    }

    /** Roles "canonicos" en MAYUSCULAS sin el prefijo ROLE_, para decidir la vista destino. */
    public Set<String> getRolesPlanos() {
        return usuario.getRoles().stream()
                .map(r -> normaliza(r.getNombreRol()))
                .collect(Collectors.toSet());
    }
}
