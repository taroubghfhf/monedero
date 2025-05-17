package co.edu.uniquindio.monedero.infraestructura.arbolbalanceado;

import co.edu.uniquindio.monedero.dominio.modelo.RangoCliente;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodoRango {
    private String cedulaCliente;
    private int puntos;
    private RangoCliente rango;
    private NodoRango izquierdo;
    private NodoRango derecho;
    private int altura;

    public NodoRango(String cedulaCliente, int puntos) {
        this.cedulaCliente = cedulaCliente;
        this.puntos = puntos;
        this.rango = RangoCliente.obtenerRangoPorPuntos(puntos);
        this.altura = 1;
        this.izquierdo = null;
        this.derecho = null;
    }
} 