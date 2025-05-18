package co.edu.uniquindio.monedero.infraestructura.cola;

import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodoCola {
    private TransaccionProgramada transaccion;
    private NodoCola siguiente;

    public NodoCola(TransaccionProgramada transaccion) {
        this.transaccion = transaccion;
        this.siguiente = null;
    }
} 