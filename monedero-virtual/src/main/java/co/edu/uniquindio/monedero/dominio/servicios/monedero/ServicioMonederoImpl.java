package co.edu.uniquindio.monedero.dominio.servicios.monedero;

import co.edu.uniquindio.monedero.dominio.modelo.*;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.infraestructura.grafo.Arista;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ServicioMonederoImpl implements ServicioMonedero {
    
    @Autowired
    private CuentaDao cuentaDao;
    
    public ServicioMonederoImpl() {
        System.out.println("DEBUG: ServicioMonederoImpl constructor called");
    }
    
    @Override
    public Monedero crearMonedero(String cedulaCliente, String nombre, TipoMonedero tipo) {
        System.out.println("DEBUG: Buscando cuenta para cliente: " + cedulaCliente);
        System.out.println("DEBUG: CuentaDao instance: " + cuentaDao.getClass().getName());
        
        // Verificar si existe el cliente primero
        boolean existeCuenta = cuentaDao.existeCuenta(cedulaCliente);
        System.out.println("DEBUG: Existe cuenta: " + existeCuenta);
        
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        System.out.println("DEBUG: Cuenta encontrada: " + (cuenta != null ? cuenta.getNumeroCuenta() : "null"));
        
        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada para el cliente: " + cedulaCliente);
        }
        
        String idMonedero = UUID.randomUUID().toString();
        Monedero nuevoMonedero = new Monedero(idMonedero, nombre, tipo, cuenta.getNumeroCuenta());
        
        cuenta.agregarMonedero(nuevoMonedero);
        
        // Crear relación bidireccional con el monedero principal (comisión 0)
        Monedero monederoPrincipal = buscarMonederoPrincipal(cuenta);
        if (monederoPrincipal != null && !monederoPrincipal.getId().equals(idMonedero)) {
            cuenta.crearRelacionEntreMonederos(monederoPrincipal.getId(), idMonedero, 0.0);
            cuenta.crearRelacionEntreMonederos(idMonedero, monederoPrincipal.getId(), 0.0);
        }
        
        return nuevoMonedero;
    }
    
    @Override
    public TransaccionEntreMonederos transferirEntreMonederos(
            String cedulaCliente, 
            String idMonederoOrigen, 
            String idMonederoDestino, 
            double monto, 
            String concepto) {
        
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada para el cliente: " + cedulaCliente);
        }
        
        Monedero monederoOrigen = buscarMonederoPorId(cedulaCliente, idMonederoOrigen);
        Monedero monederoDestino = buscarMonederoPorId(cedulaCliente, idMonederoDestino);
        
        if (monederoOrigen == null || monederoDestino == null) {
            throw new RuntimeException("Uno o ambos monederos no encontrados");
        }
        
        if (monederoOrigen.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente en el monedero origen");
        }
        
        // Verificar si existe relación directa o buscar camino óptimo
        List<String> camino = obtenerCaminoOptimo(cedulaCliente, idMonederoOrigen, idMonederoDestino);
        if (camino.isEmpty()) {
            throw new RuntimeException("No existe camino entre los monederos");
        }
        
        // Realizar la transferencia
        monederoOrigen.setSaldo(monederoOrigen.getSaldo() - monto);
        monederoDestino.setSaldo(monederoDestino.getSaldo() + monto);
        
        // Crear transacción
        String codigoTransaccion = UUID.randomUUID().toString();
        TransaccionEntreMonederos transaccion = new TransaccionEntreMonederos(
            codigoTransaccion, monto, LocalDateTime.now(), 
            idMonederoOrigen, idMonederoDestino, concepto
        );
        
        cuenta.getTransacciones().insertarAlFinal(transaccion);
        
        return transaccion;
    }
    
    @Override
    public List<Monedero> obtenerMonederosPorCliente(String cedulaCliente) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada para el cliente: " + cedulaCliente);
        }
        
        List<Monedero> monederos = new ArrayList<>();
        cuenta.getMonederos().recorrer(monederos::add);
        return monederos;
    }
    
    @Override
    public Monedero buscarMonederoPorId(String cedulaCliente, String idMonedero) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            return null;
        }
        
        try {
            return cuenta.getMonederos().buscar(m -> m.getId().equals(idMonedero));
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public List<String> obtenerCaminoOptimo(String cedulaCliente, String idOrigen, String idDestino) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            return new ArrayList<>();
        }
        
        return cuenta.getGrafoMonederos().obtenerCaminoMasCorto(idOrigen, idDestino);
    }
    
    @Override
    public void establecerRelacionMonederos(String cedulaCliente, String idOrigen, String idDestino, double comision) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada para el cliente: " + cedulaCliente);
        }
        
        cuenta.crearRelacionEntreMonederos(idOrigen, idDestino, comision);
    }
    
    @Override
    public List<Arista<String>> obtenerRelacionesMonedero(String cedulaCliente, String idMonedero) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            return new ArrayList<>();
        }
        
        return cuenta.getGrafoMonederos().obtenerAdyacentes(idMonedero);
    }
    
    @Override
    public void eliminarMonedero(String cedulaCliente, String idMonedero) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada para el cliente: " + cedulaCliente);
        }
        
        Monedero monedero = buscarMonederoPorId(cedulaCliente, idMonedero);
        if (monedero == null) {
            throw new RuntimeException("Monedero no encontrado");
        }
        
        if (monedero.getTipo() == TipoMonedero.PRINCIPAL) {
            throw new RuntimeException("No se puede eliminar el monedero principal");
        }
        
        if (monedero.getSaldo() > 0) {
            throw new RuntimeException("No se puede eliminar un monedero con saldo");
        }
        
        cuenta.getMonederos().eliminar(monedero);
        cuenta.getGrafoMonederos().eliminarVertice(idMonedero);
    }
    
    @Override
    public double calcularSaldoTotalMonederos(String cedulaCliente) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            return 0.0;
        }
        
        return cuenta.calcularSaldoTotal();
    }
    
    @Override
    public void transferirDeCuentaAMonedero(String cedulaCliente, String idMonedero, double monto) {
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada para el cliente: " + cedulaCliente);
        }
        
        if (monto <= 0) {
            throw new RuntimeException("El monto debe ser mayor a cero");
        }
        
        if (cuenta.getSaldoCuenta() < monto) {
            throw new RuntimeException("Saldo insuficiente en la cuenta. Saldo disponible: " + cuenta.getSaldoCuenta());
        }
        
        Monedero monedero = buscarMonederoPorId(cedulaCliente, idMonedero);
        if (monedero == null) {
            throw new RuntimeException("Monedero no encontrado");
        }
        
        // Transferir dinero de la cuenta al monedero
        cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() - monto);
        monedero.setSaldo(monedero.getSaldo() + monto);
        
        // Crear transacción
        String codigoTransaccion = UUID.randomUUID().toString();
        TransaccionMonedero transaccion = new TransaccionMonedero(monto);
        transaccion.setId(codigoTransaccion);
        transaccion.setNombre("Transferencia de cuenta a monedero: " + monedero.getNombre());
        
        cuenta.getTransacciones().insertarAlFinal(transaccion);
    }
    
    private Monedero buscarMonederoPrincipal(Cuenta cuenta) {
        try {
            return cuenta.getMonederos().buscar(m -> m.getTipo() == TipoMonedero.PRINCIPAL);
        } catch (Exception e) {
            return null;
        }
    }
} 