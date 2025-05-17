package co.edu.uniquindio.monedero.aplicacion.comando;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CuentaComando {
    private String cedulaCliente;

    public CuentaComando(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }
}
