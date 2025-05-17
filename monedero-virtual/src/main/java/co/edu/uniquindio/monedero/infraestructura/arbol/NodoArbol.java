package co.edu.uniquindio.monedero.infraestructura.arbol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodoArbol {
    private String cedulaCliente;
    private int puntos;
    private NodoArbol izquierdo;
    private NodoArbol derecho;

    public NodoArbol(String cedulaCliente, int puntos) {
        this.cedulaCliente = cedulaCliente;
        this.puntos = puntos;
        this.izquierdo = null;
        this.derecho = null;
    }
} 