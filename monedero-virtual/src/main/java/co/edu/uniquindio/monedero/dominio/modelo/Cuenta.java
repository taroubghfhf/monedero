package co.edu.uniquindio.monedero.dominio.modelo;

import co.edu.uniquindio.monedero.infraestructura.listasimple.ListaSimple;
import co.edu.uniquindio.monedero.infraestructura.grafo.Grafo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

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
    private Grafo<String> grafoMonederos; // Grafo dirigido de relaciones entre monederos

    public Cuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
        this.saldoCuenta = 0.0;
        this.saldoTotal = 0.0;
        this.totalPuntos = 0;
        this.transacciones = new ListaSimple<>();
        this.monederos = new ListaSimple<>();
        this.grafoMonederos = new Grafo<>();
        
        // Crear monedero principal por defecto
        crearMonederoPrincipal();
    }
    
    private void crearMonederoPrincipal() {
        String idMonederoPrincipal = UUID.randomUUID().toString();
        Monedero monederoPrincipal = new Monedero(
            idMonederoPrincipal, 
            "Principal", 
            TipoMonedero.PRINCIPAL,
            this.numeroCuenta
        );
        this.monederos.insertarAlFinal(monederoPrincipal);
        this.grafoMonederos.agregarVertice(idMonederoPrincipal);
    }
    
    public void agregarMonedero(Monedero monedero) {
        this.monederos.insertarAlFinal(monedero);
        this.grafoMonederos.agregarVertice(monedero.getId());
    }
    
    public void crearRelacionEntreMonederos(String idOrigen, String idDestino, double comision) {
        this.grafoMonederos.agregarArista(idOrigen, idDestino, comision);
    }
    
    public double calcularSaldoTotal() {
        final double[] total = {this.saldoCuenta};
        this.monederos.recorrer(monedero -> total[0] += monedero.getSaldo());
        this.saldoTotal = total[0];
        return total[0];
    }
}
