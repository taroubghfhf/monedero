package co.edu.uniquindio.monedero.infraestructura.listasimple;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ListaSimple<T> {

    private ListaSimpleNodo<T> cabeza;

    public boolean estaVacia() {
        return cabeza == null;
    }

    public void insertarAlInicio(T dato) {
        ListaSimpleNodo<T> nuevo = new ListaSimpleNodo<>(dato);
        nuevo.setSiguiente(cabeza);
        cabeza = nuevo;
    }

    public void insertarAlFinal(T dato) {
        ListaSimpleNodo<T> nuevo = new ListaSimpleNodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
            return;
        }
        ListaSimpleNodo<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            actual = actual.getSiguiente();
        }
        actual.setSiguiente(nuevo);
    }

    public T buscar(Predicate<T> predicado) {
        ListaSimpleNodo<T> actual = cabeza;
        while (actual != null) {
            if (predicado.test(actual.getDato())) {
                return actual.getDato();
            }
            actual = actual.getSiguiente();
        }
        throw new NoSuchElementException("No se encontró el elemento");
    }

    public void eliminar(T dato) {
        if (estaVacia()) {
            throw new NoSuchElementException("Lista vacía");
        }
        if (cabeza.getDato().equals(dato)) {
            cabeza = cabeza.getSiguiente();
            return;
        }
        ListaSimpleNodo<T> actual = cabeza;
        while (actual.getSiguiente() != null && !actual.getSiguiente().getDato().equals(dato)) {
            actual = actual.getSiguiente();
        }
        if (actual.getSiguiente() == null) {
            throw new NoSuchElementException("No encontrado: " + dato);
        }
        actual.setSiguiente(actual.getSiguiente().getSiguiente());
    }

    public void recorrer(Consumer<T> consumer) {
        ListaSimpleNodo<T> actual = cabeza;
        while (actual != null) {
            consumer.accept(actual.getDato());
            actual = actual.getSiguiente();
        }
    }

    public ListaSimpleNodo<T> getCabeza() {
        return cabeza;
    }
}
