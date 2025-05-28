package co.edu.uniquindio.monedero.aplicacion.manejador.cliente;

import co.edu.uniquindio.monedero.aplicacion.comando.ClienteComando;
import co.edu.uniquindio.monedero.aplicacion.fabrica.cliente.FabricaCliente;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.CrearClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManejadorCrearCliente {

    private final CrearClienteService crearClienteService;

    public void ejecutar(ClienteComando clienteComando) {
        Cliente cliente = FabricaCliente.crearCliente(clienteComando);
        crearClienteService.crearCliente(cliente);
    }
}