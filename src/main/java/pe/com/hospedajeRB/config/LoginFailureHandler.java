package pe.com.hospedajeRB.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import pe.com.hospedajeRB.seguridad.service.IntentosService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Maneja el login fallido:
 *  - RN08 / RNF04: incrementa intentos fallidos (y bloquea al superar el limite).
 *  - Distingue el motivo para mostrar el mensaje correcto en la vista:
 *      bloqueado / inactivo / credenciales.
 *  - RNF18: deja traza del intento fallido.
 *
 * Nota de seguridad: por defecto NO revelamos si el error fue "usuario" o
 * "password" (evita enumeracion de usuarios). El diseno muestra el bloque rojo
 * generico "Usuario o contrasena incorrectos".
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);
    private final IntentosService intentosService;

    public LoginFailureHandler(IntentosService intentosService) {
        this.intentosService = intentosService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String username = request.getParameter("username");
        String motivo;

        if (exception instanceof LockedException) {
            motivo = "bloqueado";
        } else if (exception instanceof DisabledException) {
            motivo = "inactivo";
        } else {
            // BadCredentials o UsernameNotFound: cuenta como intento fallido.
            if (username != null && !username.isBlank()) {
                intentosService.registrarFallo(username);
            }
            motivo = "credenciales";
        }

        log.warn("RNF18 - login FALLIDO usuario='{}' motivo={}", username, motivo);
        String enc = URLEncoder.encode(motivo, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?error=" + enc);
    }
}
