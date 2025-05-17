package co.edu.uniquindio.monedero.aplicacion.fabrica.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionDepositoComando;
import co.edu.uniquindio.monedero.aplicacion.comando.TransaccionRetiroComando;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionDeposito;
import co.edu.uniquindio.monedero.dominio.modelo.TransaccionRetiro;

public class FabricaTransaccion {

    public static TransaccionDeposito crear(TransaccionDepositoComando comando) {
        if (comando == null) {
            throw new IllegalArgumentException("El comando de transacción no puede ser nulo");
        }
        return new TransaccionDeposito(comando.getMonto());
    }

    public static TransaccionRetiro crear(TransaccionRetiroComando comando) {
        if (comando == null) {
            throw new IllegalArgumentException("El comando de transacción no puede ser nulo");
        }
        return new TransaccionRetiro(comando.getMonto());
    }
}
