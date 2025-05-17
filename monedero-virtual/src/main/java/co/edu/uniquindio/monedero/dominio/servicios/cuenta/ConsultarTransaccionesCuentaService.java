package co.edu.uniquindio.monedero.dominio.servicios.cuenta;

import co.edu.uniquindio.monedero.dominio.dto.transaccion.TransaccionDTO;
import co.edu.uniquindio.monedero.dominio.exception.NoExisteClienteException;
import co.edu.uniquindio.monedero.dominio.modelo.Cuenta;
import co.edu.uniquindio.monedero.dominio.modelo.Transaccion;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;
import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimpleNodo;
import org.springframework.stereotype.Service;

@Service
public class ConsultarTransaccionesCuentaService {

    private final CuentaDao cuentaDao;

    public ConsultarTransaccionesCuentaService(CuentaDao cuentaDao) {
        this.cuentaDao = cuentaDao;
    }

    public ListaSimple<TransaccionDTO> ejecutar(String numeroCuenta) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(numeroCuenta);
        if (cuenta == null) {
            throw new NoExisteClienteException("No existe la cuenta con el número: " + numeroCuenta);
        }

        ListaSimple<Transaccion> transacciones = cuenta.getTransacciones();
        if (transacciones == null || transacciones.estaVacia()) {
            throw new IllegalArgumentException("No hay transacciones para mostrar");
        }

        ListaSimple<TransaccionDTO> transaccionesDTO = new ListaSimple<>();
        ListaSimpleNodo<Transaccion> nodoActual = transacciones.getCabeza();

        while (nodoActual != null) {
            Transaccion transaccion = nodoActual.getDato();
            TransaccionDTO dto = new TransaccionDTO(
                    transaccion.getId(),
                    transaccion.getMonto(),
                    transaccion.getFecha(),
                    transaccion.getTipoTransaccion()
            );
            transaccionesDTO.insertarAlFinal(dto);
            nodoActual = nodoActual.getSiguiente();
        }

        return transaccionesDTO;
    }
}
