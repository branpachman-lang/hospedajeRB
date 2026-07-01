package pe.com.hospedajeRB.seguridad.service;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface RecuperacionService {

    void solicitar(String correo);

    boolean tokenValido(String token);

    boolean restablecer(String token, String nuevaPassword);

}
