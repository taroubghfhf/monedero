package co.edu.uniquindio.monedero.infraestructura.adaptador.cuenta;

import co.edu.uniquindio.monedero.dominio.dto.cuenta.CuentaDTO;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.infraestructura.persistencia.Lista;
import org.springframework.stereotype.Component;

@Component
public class CuentaImplementacionDAO implements CuentaDao {


    Lista listaClientes;

    public CuentaImplementacionDAO() {
        this.listaClientes = Lista.obtenerLista();
    }

    @Override
    public CuentaDTO buscarPorNumeroCuentaNumeroCedula( String cedulaCliente) {
        Cliente cliente = listaClientes.buscarCliente(cedulaCliente);
        if (cliente == null){
            return null;
        }

        if (cliente.getCuenta() == null) {
            return null;
        }

        return new CuentaDTO(cliente.getCuenta().getNumeroCuenta(),
                cliente.getCuenta().getSaldoCuenta(), cliente.getCuenta().getSaldoTotal(),
                cliente.getCuenta().getTotalPuntos(), cliente.getCedula());
    }

    @Override
    public boolean existeCuenta(String cedulaCliente) {
        Cliente cliente = listaClientes.buscarCliente(cedulaCliente);
        if (cliente == null) {
            return false;
        }
        return cliente.getCuenta() != null;
    }

    @Override
    public Cuenta buscarPorNumeroCuenta(String cedulaCliente) {
        Cliente cliente = listaClientes.buscarCliente(cedulaCliente);
        if (cliente != null && cliente.getCuenta() != null) {
            return cliente.getCuenta();
        }
        return null;
    }

}
