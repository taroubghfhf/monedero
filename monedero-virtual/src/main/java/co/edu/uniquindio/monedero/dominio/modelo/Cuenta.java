package co.edu.uniquindio.monedero.dominio.modelo;

import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {
    private String numeroCuenta;
    private double saldoCuenta;
    private double saldoTotal;
    private int totalPuntos;
    private ListaSimple<Transaccion> transacciones;
    private ListaSimple<Monedero> monederos;
}
