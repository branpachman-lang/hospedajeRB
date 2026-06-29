package pe.com.hospedajeRB.web;

import pe.com.hospedajeRB.seguridad.security.UsuarioPrincipal;

import java.util.List;
import java.util.Set;

/** Resuelve la vista de inicio (home) que corresponde a cada rol. */
public final class RolHome {

    private RolHome() {}

    private static final List<String[]> RUTAS = List.of(
            new String[]{"GERENTE",        "/gerente"},
            new String[]{"RECEPCIONISTA",  "/recepcionista"},
            new String[]{"MANTENIMIENTO",  "/mantenimiento"},
            new String[]{"LIMPIEZA",       "/limpieza"}
    );

    public static String resolver(UsuarioPrincipal principal) {
        Set<String> roles = principal.getRolesPlanos();
        for (String[] par : RUTAS) {
            if (roles.contains(par[0])) {
                return par[1];
            }
        }
        return "/login";
    }
}
