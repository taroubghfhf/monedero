package co.edu.uniquindio.monedero.dominio.servicios.monedero;

import co.edu.uniquindio.monedero.dominio.modelo.*;
import co.edu.uniquindio.monedero.infraestructura.grafo.Arista;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ServicioMonedero {
    
    /**
     * Crea un nuevo monedero para una cuenta
     */
    Monedero crearMonedero(String cedulaCliente, String nombre, TipoMonedero tipo);
    
    /**
     * Transfiere dinero entre monederos de la misma cuenta
     */
    TransaccionEntreMonederos transferirEntreMonederos(
        String cedulaCliente, 
        String idMonederoOrigen, 
        String idMonederoDestino, 
        double monto, 
        String concepto
    );
    
    /**
     * Obtiene todos los monederos de una cuenta
     */
    List<Monedero> obtenerMonederosPorCliente(String cedulaCliente);
    
    /**
     * Busca un monedero por su ID
     */
    Monedero buscarMonederoPorId(String cedulaCliente, String idMonedero);
    
    /**
     * Obtiene el camino más eficiente entre dos monederos (menor comisión)
     */
    List<String> obtenerCaminoOptimo(String cedulaCliente, String idOrigen, String idDestino);
    
    /**
     * Establece una relación directa entre dos monederos con una comisión
     */
    void establecerRelacionMonederos(String cedulaCliente, String idOrigen, String idDestino, double comision);
    
    /**
     * Obtiene las relaciones de un monedero
     */
    List<Arista<String>> obtenerRelacionesMonedero(String cedulaCliente, String idMonedero);
    
    /**
     * Elimina un monedero (solo si tiene saldo 0)
     */
    void eliminarMonedero(String cedulaCliente, String idMonedero);
    
    /**
     * Calcula el saldo total de todos los monederos de una cuenta
     */
    double calcularSaldoTotalMonederos(String cedulaCliente);
    
    /**
     * Transfiere dinero del saldo de la cuenta a un monedero
     */
    void transferirDeCuentaAMonedero(String cedulaCliente, String idMonedero, double monto);
} 