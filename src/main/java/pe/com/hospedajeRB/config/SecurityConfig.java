package pe.com.hospedajeRB.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad central. Roles: GERENTE, RECEPCIONISTA, LIMPIEZA, MANTENIMIENTO.
 *  RNF03 BCrypt | RF40/RNF06/RN12 RBAC | RF35 login | RN08 bloqueo (handlers) | RN10 inactividad.
 */
@Configuration
public class SecurityConfig {

    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;

    public SecurityConfig(LoginSuccessHandler successHandler, LoginFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/recuperar", "/recuperar/**",
                                 "/restablecer", "/restablecer/**").permitAll()
                .requestMatchers("/cargando", "/cambio-password", "/logout").authenticated()
                // RBAC por rol (4 roles)
                .requestMatchers("/gerente/**").hasRole("GERENTE")
                .requestMatchers("/recepcionista/**").hasRole("RECEPCIONISTA")
                .requestMatchers("/limpieza/**").hasRole("LIMPIEZA")
                .requestMatchers("/mantenimiento/**").hasRole("MANTENIMIENTO")
                .requestMatchers("/usuarios/**", "/configuracion/**").hasRole("GERENTE")
                .anyRequest().authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler))

            .logout(out -> out
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))

            // RN10: al expirar la sesion por inactividad, vuelve al login con aviso.
            .sessionManagement(sm -> sm
                .invalidSessionUrl("/login?expirada"));

        // CSRF queda ACTIVO (default). Thymeleaf inyecta el token en cada form.
        return http.build();
    }
}
