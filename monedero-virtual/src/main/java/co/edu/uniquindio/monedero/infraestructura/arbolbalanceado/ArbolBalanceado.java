package co.edu.uniquindio.monedero.infraestructura.arbolbalanceado;

import co.edu.uniquindio.monedero.dominio.modelo.RangoCliente;

public class ArbolBalanceado {
    private NodoRango raiz;

    public ArbolBalanceado() {
        this.raiz = null;
    }

    public void actualizarPuntos(String cedulaCliente, int puntos) {
        raiz = actualizarPuntosRecursivo(raiz, cedulaCliente, puntos);
    }

    private NodoRango actualizarPuntosRecursivo(NodoRango nodo, String cedulaCliente, int puntos) {
        if (nodo == null) {
            return new NodoRango(cedulaCliente, puntos);
        }

        int comparacion = cedulaCliente.compareTo(nodo.getCedulaCliente());
        
        if (comparacion < 0) {
            nodo.setIzquierdo(actualizarPuntosRecursivo(nodo.getIzquierdo(), cedulaCliente, puntos));
        } else if (comparacion > 0) {
            nodo.setDerecho(actualizarPuntosRecursivo(nodo.getDerecho(), cedulaCliente, puntos));
        } else {
            nodo.setPuntos(puntos);
            nodo.setRango(RangoCliente.obtenerRangoPorPuntos(puntos));
            return nodo;
        }

        actualizarAltura(nodo);
        return balancearNodo(nodo, cedulaCliente);
    }

    private void actualizarAltura(NodoRango nodo) {
        nodo.setAltura(1 + Math.max(
            obtenerAltura(nodo.getIzquierdo()),
            obtenerAltura(nodo.getDerecho())
        ));
    }

    private NodoRango balancearNodo(NodoRango nodo, String cedulaCliente) {
        int balance = obtenerBalance(nodo);

        if (balance > 1 && cedulaCliente.compareTo(nodo.getIzquierdo().getCedulaCliente()) < 0) {
            return rotacionDerecha(nodo);
        }

        if (balance < -1 && cedulaCliente.compareTo(nodo.getDerecho().getCedulaCliente()) > 0) {
            return rotacionIzquierda(nodo);
        }

        if (balance > 1 && cedulaCliente.compareTo(nodo.getIzquierdo().getCedulaCliente()) > 0) {
            nodo.setIzquierdo(rotacionIzquierda(nodo.getIzquierdo()));
            return rotacionDerecha(nodo);
        }

        if (balance < -1 && cedulaCliente.compareTo(nodo.getDerecho().getCedulaCliente()) < 0) {
            nodo.setDerecho(rotacionDerecha(nodo.getDerecho()));
            return rotacionIzquierda(nodo);
        }

        return nodo;
    }

    public RangoCliente obtenerRango(String cedulaCliente) {
        NodoRango nodo = buscarNodo(raiz, cedulaCliente);
        return nodo != null ? nodo.getRango() : RangoCliente.BRONCE;
    }

    public NodoRango buscarNodo(String cedulaCliente) {
        return buscarNodo(raiz, cedulaCliente);
    }

    private NodoRango buscarNodo(NodoRango nodo, String cedulaCliente) {
        if (nodo == null) {
            return null;
        }

        int comparacion = cedulaCliente.compareTo(nodo.getCedulaCliente());
        
        if (comparacion < 0) {
            return buscarNodo(nodo.getIzquierdo(), cedulaCliente);
        } else if (comparacion > 0) {
            return buscarNodo(nodo.getDerecho(), cedulaCliente);
        }
        
        return nodo;
    }

    private int obtenerAltura(NodoRango nodo) {
        return nodo == null ? 0 : nodo.getAltura();
    }

    private int obtenerBalance(NodoRango nodo) {
        return nodo == null ? 0 : obtenerAltura(nodo.getIzquierdo()) - obtenerAltura(nodo.getDerecho());
    }

    private NodoRango rotacionDerecha(NodoRango y) {
        NodoRango x = y.getIzquierdo();
        NodoRango T2 = x.getDerecho();

        x.setDerecho(y);
        y.setIzquierdo(T2);

        actualizarAltura(y);
        actualizarAltura(x);

        return x;
    }

    private NodoRango rotacionIzquierda(NodoRango x) {
        NodoRango y = x.getDerecho();
        NodoRango T2 = y.getIzquierdo();

        y.setIzquierdo(x);
        x.setDerecho(T2);

        actualizarAltura(x);
        actualizarAltura(y);

        return y;
    }
} 