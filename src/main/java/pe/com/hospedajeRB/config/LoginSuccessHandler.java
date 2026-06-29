package pe.com.hospedajeRB.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;
import pe.com.hospedajeRB.seguridad.service.IntentosService;

import java.io.IOException;

/**
 * Maneja el login correcto:
 *  - RN08: reinicia el contador de intentos fallidos.
 *  - RNF18: deja traza (log) de quien y cuando inicio sesion.
 *  - RF37: si la credencial es temporal, va directo a cambiar password.
 *  - Si no, pasa por la pantalla de carga (/cargando) que luego redirige por rol.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);
    private final IntentosService intentosService;

    public LoginSuccessHandler(IntentosService intentosService) {
        this.intentosService = intentosService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        intentosService.registrarExito(principal.getUsername());
        log.info("RNF18 - login OK usuario='{}' roles={}",
                principal.getUsername(), principal.getRolesPlanos());

        if (principal.esTemporal()) {
            // RF36/RF37: bloquea el resto del sistema hasta cambiar la clave temporal.
            response.sendRedirect(request.getContextPath() + "/cambio-password");
        } else {
            response.sendRedirect(request.getContextPath() + "/cargando");
        }
    }
}
