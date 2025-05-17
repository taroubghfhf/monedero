package co.edu.uniquindio.monedero.infraestructura.persistencia;

import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;

import java.util.NoSuchElementException;

public class Lista {

    private static Lista instancia;
    private final ListaSimple<Cliente> clientes;

    private Lista() {
        this.clientes = new ListaSimple<>();
    }

    public static synchronized Lista obtenerLista() {
        if (instancia == null) {
            instancia = new Lista();
        }
        return instancia;
    }

    public void agregarCliente(Cliente cliente) {
        clientes.insertarAlFinal(cliente);
    }

    public Cliente buscarCliente(String cedula) {
        Cliente clienteMonedero = null;
        try {
            clienteMonedero = clientes.buscar(cliente -> cliente.getCedula().equals(cedula));
        } catch (NoSuchElementException e) {
            return null;
        }
        return clienteMonedero;
    }

}