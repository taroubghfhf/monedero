package co.edu.uniquindio.monedero.infraestructura.adaptador.cuenta;

import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import co.edu.uniquindio.monedero.infraestructura.persistencia.Lista;
import org.springframework.stereotype.Repository;

@Repository
public class CuentaImplementacionRepositorio implements CuentaRepositorio {

    Lista listaClientes;

    public CuentaImplementacionRepositorio() {
        this.listaClientes = Lista.obtenerLista();
    }


    @Override
    public boolean agregarCuenta(Cuenta cuenta, String cedulaCliente) {
        Cliente cliente = listaClientes.buscarCliente(cedulaCliente);
        if (cliente != null) {
            cliente.setCuenta(cuenta);
            return true;
        }
        return false;
    }

    @Override
    public Cuenta buscarPorNumeroCuenta(String numeroCuenta) {
        return null;
    }

    @Override
    public boolean actualizar(Cuenta cuenta) {
        return false;
    }
}
