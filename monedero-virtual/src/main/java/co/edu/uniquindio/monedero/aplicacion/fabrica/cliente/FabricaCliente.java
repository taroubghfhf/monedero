package co.edu.uniquindio.monedero.aplicacion.fabrica.cliente;

import co.edu.uniquindio.monedero.aplicacion.comando.ClienteComando;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;

public class FabricaCliente {

    public static Cliente crearCliente(ClienteComando comando) {
        if (comando == null) {
            throw new IllegalArgumentException("El comando de cliente no puede ser nulo");
        }
        return new Cliente(
            comando.getNombre(),
            comando.getApellido(),
            comando.getCedula(),
            comando.getCorreo(),
            comando.getTelefono()
        );
    }
}