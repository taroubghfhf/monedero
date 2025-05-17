package co.edu.uniquindio.monedero.dominio.servicios.puntos;

import co.edu.uniquindio.monedero.dominio.modelo.*;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.infraestructura.arbol.ArbolPuntos;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GestorPuntosService {
    private static final int PUNTOS_POR_CIEN_DEPOSITO = 1;
    private static final int PUNTOS_POR_CIEN_RETIRO = 2;
    private static final int PUNTOS_POR_CIEN_TRANSFERENCIA = 3;
    private static final double VALOR_BASE = 100.0;

    private final ArbolPuntos arbolPuntos;
    private final Map<String, List<BeneficioActivo>> beneficiosActivos;
    private final CuentaDao cuentaDao;

    public GestorPuntosService(CuentaDao cuentaDao) {
        this.arbolPuntos = new ArbolPuntos();
        this.beneficiosActivos = new HashMap<>();
        this.cuentaDao = cuentaDao;
    }

    public int calcularYAgregarPuntos(String cedulaCliente, double monto, TipoTransaccion tipoTransaccion) {
        validarDatosCalculoPuntos(cedulaCliente, monto);
        int puntosBase = calcularPuntosBase(monto, tipoTransaccion);
        
        // Obtener puntos actuales y sumar los nuevos
        int puntosActuales = arbolPuntos.obtenerPuntos(cedulaCliente);
        int nuevosPuntos = puntosActuales + puntosBase;
        
        // Actualizar puntos en el árbol
        arbolPuntos.establecerPuntos(cedulaCliente, nuevosPuntos);
        
        // Actualizar puntos en la cuenta
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta != null) {
            cuenta.setTotalPuntos(nuevosPuntos);
        }
        
        return puntosBase;
    }

    private void validarDatosCalculoPuntos(String cedulaCliente, double monto) {
        if (cedulaCliente == null || cedulaCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula del cliente no puede ser nula o vacía");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private int calcularPuntosBase(double monto, TipoTransaccion tipoTransaccion) {
        // Redondear hacia abajo para obtener unidades completas de 100
        int unidadesDeCien = (int) Math.floor(monto / VALOR_BASE);
        
        return switch (tipoTransaccion) {
            case DEPOSITO -> unidadesDeCien * PUNTOS_POR_CIEN_DEPOSITO;
            case RETIRO -> unidadesDeCien * PUNTOS_POR_CIEN_RETIRO;
            case TRANSFERENCIA_SALIENTE -> unidadesDeCien * PUNTOS_POR_CIEN_TRANSFERENCIA;
            default -> 0;
        };
    }

    public int consultarPuntos(String cedulaCliente) {
        validarCedulaCliente(cedulaCliente);
        return arbolPuntos.obtenerPuntos(cedulaCliente);
    }

    public RangoCliente consultarRango(String cedulaCliente) {
        validarCedulaCliente(cedulaCliente);
        int puntos = consultarPuntos(cedulaCliente);
        return RangoCliente.obtenerRangoPorPuntos(puntos);
    }

    private void validarCedulaCliente(String cedulaCliente) {
        if (cedulaCliente == null || cedulaCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula del cliente no puede ser nula o vacía");
        }
    }

    public boolean canjearPuntos(String cedulaCliente, int puntos) {
        validarDatosCanje(cedulaCliente, puntos);
        
        int puntosActuales = arbolPuntos.obtenerPuntos(cedulaCliente);
        TipoBeneficio beneficio = TipoBeneficio.obtenerPorPuntos(puntos);

        if (beneficio == null || puntosActuales < puntos) {
            return false;
        }

        // Aplicar el beneficio y actualizar puntos en el árbol
        aplicarBeneficio(cedulaCliente, beneficio);
        arbolPuntos.agregarPuntos(cedulaCliente, -puntos);

        // Actualizar puntos en la cuenta usando la cédula del cliente
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta != null) {
            int puntosRestantes = arbolPuntos.obtenerPuntos(cedulaCliente);
            cuenta.setTotalPuntos(puntosRestantes);
        }

        return true;
    }

    private void validarDatosCanje(String cedulaCliente, int puntos) {
        validarCedulaCliente(cedulaCliente);
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos a canjear deben ser mayores a cero");
        }
    }

    private void aplicarBeneficio(String cedulaCliente, TipoBeneficio beneficio) {
        BeneficioActivo nuevoBeneficio = new BeneficioActivo(cedulaCliente, beneficio);
        beneficiosActivos.computeIfAbsent(cedulaCliente, k -> new ArrayList<>())
                        .add(nuevoBeneficio);

        if (beneficio == TipoBeneficio.BONO_SALDO) {
            aplicarBonoSaldo(cedulaCliente, beneficio.getValor());
        }
    }

    private void aplicarBonoSaldo(String cedulaCliente, double bono) {
        // Usar la cédula del cliente para buscar la cuenta
        Cuenta cuenta = cuentaDao.buscarPorNumeroCuenta(cedulaCliente);
        if (cuenta != null) {
            cuenta.setSaldoCuenta(cuenta.getSaldoCuenta() + bono);
            cuenta.setSaldoTotal(cuenta.getSaldoTotal() + bono);
        }
    }

    public double obtenerDescuentoTransferencia(String cedulaCliente) {
        return beneficiosActivos.getOrDefault(cedulaCliente, new ArrayList<>()).stream()
            .filter(b -> b.getTipoBeneficio() == TipoBeneficio.REDUCCION_COMISION && b.estaVigente())
            .findFirst()
            .map(b -> b.getTipoBeneficio().getValor() / 100.0)
            .orElse(0.0);
    }

    public boolean tieneExencionRetiros(String cedulaCliente) {
        return beneficiosActivos.getOrDefault(cedulaCliente, new ArrayList<>()).stream()
            .anyMatch(b -> b.getTipoBeneficio() == TipoBeneficio.SIN_CARGOS_RETIROS && b.estaVigente());
    }
} 