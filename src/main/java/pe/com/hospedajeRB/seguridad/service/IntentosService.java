package pe.com.hospedajeRB.seguridad.service;

/** Contrato del servicio. Implementacion en el paquete impl. */
public interface IntentosService {

    void registrarFallo(String username);

    void registrarExito(String username);

}
