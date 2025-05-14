package co.edu.uniquindio.monedero.infraestructura.listasimple;

public class ListaSimpleNodo<T> {

    private T dato;
    private ListaSimpleNodo<T> siguiente;

    public ListaSimpleNodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public ListaSimpleNodo<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(ListaSimpleNodo<T> siguiente) {
        this.siguiente = siguiente;
    }
}
