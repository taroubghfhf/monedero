package co.edu.uniquindio.monedero.infraestructura.arbol;

public class ArbolPuntos {
    private NodoArbol raiz;

    public ArbolPuntos() {
        this.raiz = null;
    }

    public void establecerPuntos(String cedulaCliente, int puntos) {
        if (raiz == null) {
            raiz = new NodoArbol(cedulaCliente, puntos);
            return;
        }
        establecerPuntosRecursivo(raiz, cedulaCliente, puntos);
    }

    private void establecerPuntosRecursivo(NodoArbol nodo, String cedulaCliente, int puntos) {
        if (cedulaCliente.equals(nodo.getCedulaCliente())) {
            nodo.setPuntos(puntos);
            return;
        }

        if (cedulaCliente.compareTo(nodo.getCedulaCliente()) < 0) {
            if (nodo.getIzquierdo() == null) {
                nodo.setIzquierdo(new NodoArbol(cedulaCliente, puntos));
            } else {
                establecerPuntosRecursivo(nodo.getIzquierdo(), cedulaCliente, puntos);
            }
        } else {
            if (nodo.getDerecho() == null) {
                nodo.setDerecho(new NodoArbol(cedulaCliente, puntos));
            } else {
                establecerPuntosRecursivo(nodo.getDerecho(), cedulaCliente, puntos);
            }
        }
    }

    public void agregarPuntos(String cedulaCliente, int puntos) {
        if (raiz == null) {
            raiz = new NodoArbol(cedulaCliente, puntos);
            return;
        }
        agregarPuntosRecursivo(raiz, cedulaCliente, puntos);
    }

    private void agregarPuntosRecursivo(NodoArbol nodo, String cedulaCliente, int puntos) {
        if (cedulaCliente.equals(nodo.getCedulaCliente())) {
            nodo.setPuntos(nodo.getPuntos() + puntos);
            return;
        }

        if (cedulaCliente.compareTo(nodo.getCedulaCliente()) < 0) {
            if (nodo.getIzquierdo() == null) {
                nodo.setIzquierdo(new NodoArbol(cedulaCliente, puntos));
            } else {
                agregarPuntosRecursivo(nodo.getIzquierdo(), cedulaCliente, puntos);
            }
        } else {
            if (nodo.getDerecho() == null) {
                nodo.setDerecho(new NodoArbol(cedulaCliente, puntos));
            } else {
                agregarPuntosRecursivo(nodo.getDerecho(), cedulaCliente, puntos);
            }
        }
    }

    public int obtenerPuntos(String cedulaCliente) {
        return obtenerPuntosRecursivo(raiz, cedulaCliente);
    }

    private int obtenerPuntosRecursivo(NodoArbol nodo, String cedulaCliente) {
        if (nodo == null) {
            return 0;
        }

        if (cedulaCliente.equals(nodo.getCedulaCliente())) {
            return nodo.getPuntos();
        }

        if (cedulaCliente.compareTo(nodo.getCedulaCliente()) < 0) {
            return obtenerPuntosRecursivo(nodo.getIzquierdo(), cedulaCliente);
        } else {
            return obtenerPuntosRecursivo(nodo.getDerecho(), cedulaCliente);
        }
    }

    public boolean canjearPuntos(String cedulaCliente, int puntosACanjear) {
        NodoArbol nodo = buscarNodo(raiz, cedulaCliente);
        if (nodo == null || nodo.getPuntos() < puntosACanjear) {
            return false;
        }
        nodo.setPuntos(nodo.getPuntos() - puntosACanjear);
        return true;
    }

    private NodoArbol buscarNodo(NodoArbol nodo, String cedulaCliente) {
        if (nodo == null || cedulaCliente.equals(nodo.getCedulaCliente())) {
            return nodo;
        }

        if (cedulaCliente.compareTo(nodo.getCedulaCliente()) < 0) {
            return buscarNodo(nodo.getIzquierdo(), cedulaCliente);
        } else {
            return buscarNodo(nodo.getDerecho(), cedulaCliente);
        }
    }
} 