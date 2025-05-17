package co.edu.uniquindio.monedero.aplicacion.comando;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransaccionDepositoComando {
   private double monto;
   private String cedulaCliente;
   private String numeroCuenta;

   public TransaccionDepositoComando(double monto, String cedulaCliente, String numeroCuenta) {
      this.monto = monto;
      this.cedulaCliente = cedulaCliente;
      this.numeroCuenta = numeroCuenta;
   }
}
