package co.edu.uniquindio.monedero.dominio.modelo;

import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class Cuenta {
    private String numeroCuenta;
    private double saldoCuenta;
    private double saldoTotal;
    private int totalPuntos;
    private ListaSimple<Transaccion> transacciones;
    private ListaSimple<Monedero> monederos;

    public Cuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
        this.saldoCuenta = 0.0;
        this.saldoTotal = 0.0;
        this.totalPuntos = 0;
        this.transacciones = new ListaSimple<>();
        this.monederos = new ListaSimple<>();
    }
}
