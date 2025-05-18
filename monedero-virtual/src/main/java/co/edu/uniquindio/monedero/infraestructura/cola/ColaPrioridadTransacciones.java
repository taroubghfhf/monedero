package co.edu.uniquindio.monedero.infraestructura.cola;

import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ColaPrioridadTransacciones {
    private NodoCola frente;
    private NodoCola final_;
    private int tamanio;

    public ColaPrioridadTransacciones() {
        this.frente = null;
        this.final_ = null;
        this.tamanio = 0;
    }

    public void agregarTransaccion(TransaccionProgramada transaccion) {
        NodoCola nuevoNodo = new NodoCola(transaccion);
        
        if (estaVacia()) {
            frente = nuevoNodo;
            final_ = nuevoNodo;
        } else {

            if (transaccion.compareTo(frente.getTransaccion()) < 0) {
                nuevoNodo.setSiguiente(frente);
                frente = nuevoNodo;
            } else {
                NodoCola actual = frente;
                NodoCola anterior = null;
                
                // Encontrar la posición correcta basada en la fecha de ejecución
                while (actual != null && 
                       transaccion.compareTo(actual.getTransaccion()) >= 0) {
                    anterior = actual;
                    actual = actual.getSiguiente();
                }
                
                // Insertar el nuevo nodo en la posición correcta
                if (actual == null) {
                    // Insertar al final
                    final_.setSiguiente(nuevoNodo);
                    final_ = nuevoNodo;
                } else {
                    // Insertar en medio
                    anterior.setSiguiente(nuevoNodo);
                    nuevoNodo.setSiguiente(actual);
                }
            }
        }
        tamanio++;
    }

    public TransaccionProgramada obtenerSiguienteTransaccion() {
        if (estaVacia()) {
            return null;
        }

        TransaccionProgramada transaccion = frente.getTransaccion();
        frente = frente.getSiguiente();
        
        if (frente == null) {
            final_ = null;
        }
        
        tamanio--;
        return transaccion;
    }

    public TransaccionProgramada verSiguienteTransaccion() {
        return estaVacia() ? null : frente.getTransaccion();
    }

    public void eliminarTransaccion(TransaccionProgramada transaccion) {
        if (estaVacia()) {
            return;
        }

        // Si es el primer elemento
        if (frente.getTransaccion().equals(transaccion)) {
            frente = frente.getSiguiente();
            if (frente == null) {
                final_ = null;
            }
            tamanio--;
            return;
        }

        // Buscar en el resto de la cola
        NodoCola actual = frente;
        NodoCola anterior = null;

        while (actual != null && !actual.getTransaccion().equals(transaccion)) {
            anterior = actual;
            actual = actual.getSiguiente();
        }

        // Si se encontró la transacción
        if (actual != null) {
            anterior.setSiguiente(actual.getSiguiente());
            if (actual == final_) {
                final_ = anterior;
            }
            tamanio--;
        }
    }

    public boolean tieneTransacciones() {
        return !estaVacia();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamanio() {
        return tamanio;
    }
} 