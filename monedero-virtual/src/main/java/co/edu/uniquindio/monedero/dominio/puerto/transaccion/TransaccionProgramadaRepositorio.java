package co.edu.uniquindio.monedero.dominio.puerto.transaccion;

import co.edu.uniquindio.monedero.dominio.modelo.TransaccionProgramada;
import java.util.List;

public interface TransaccionProgramadaRepositorio {
    void guardar(TransaccionProgramada transaccion);
    void eliminar(String id);
    TransaccionProgramada buscarPorId(String id);
    List<TransaccionProgramada> buscarPorNumeroCliente(String numeroCliente);
    List<TransaccionProgramada> obtenerTransaccionesActivas();
} 