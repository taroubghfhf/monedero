package co.edu.uniquindio.monedero.dominio.dto.cuenta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaDTO {
    private String numeroCuenta;
    private double saldoCuenta;
    private double saldoTotal;
    private int totalPuntos;
    private String numeroCliente;
}
