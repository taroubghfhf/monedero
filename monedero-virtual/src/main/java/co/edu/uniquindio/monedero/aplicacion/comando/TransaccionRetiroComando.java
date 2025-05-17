package co.edu.uniquindio.monedero.aplicacion.comando;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransaccionRetiroComando {

    private double monto;
    private String cedulaCliente;
    private String numeroCuenta;

    public TransaccionRetiroComando(double monto, String cedulaCliente, String numeroCuenta) {
        this.monto = monto;
        this.cedulaCliente = cedulaCliente;
        this.numeroCuenta = numeroCuenta;
    }
}
