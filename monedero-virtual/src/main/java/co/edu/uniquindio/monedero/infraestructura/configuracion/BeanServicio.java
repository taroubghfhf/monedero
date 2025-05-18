package co.edu.uniquindio.monedero.infraestructura.configuracion;

import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio.ClienteRepositorio;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.BuscarClienteService;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.CrearClienteService;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.*;
import co.edu.uniquindio.monedero.dominio.servicios.puntos.GestorPuntosService;
import co.edu.uniquindio.monedero.infraestructura.arbol.ArbolPuntos;
import co.edu.uniquindio.monedero.infraestructura.arbolbalanceado.ArbolBalanceado;
import co.edu.uniquindio.monedero.dominio.puerto.transaccion.TransaccionProgramadaRepositorio;
import co.edu.uniquindio.monedero.dominio.servicios.transaccion.GestorTransaccionesProgramadasService;
import co.edu.uniquindio.monedero.infraestructura.adaptador.transaccion.TransaccionProgramadaRepositorioImpl;
import co.edu.uniquindio.monedero.infraestructura.cola.ColaPrioridadTransacciones;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class BeanServicio {

    @Bean
    public ArbolPuntos arbolPuntos() {
        return new ArbolPuntos();
    }

    @Bean
    public ArbolBalanceado arbolBalanceado() {
        return new ArbolBalanceado();
    }

    @Bean
    public GestorPuntosService gestorPuntosService(CuentaDao cuentaDao) {
        return new GestorPuntosService(cuentaDao);
    }

    @Bean
    public TransaccionDepositoCuentaService transaccionDepositoCuentaService(
            CuentaDao cuentaDao,
            GestorPuntosService gestorPuntosService) {
        return new TransaccionDepositoCuentaService(cuentaDao, gestorPuntosService);
    }

    @Bean
    public TransaccionRetiroCuentaService transaccionRetiroCuentaService(
            CuentaDao cuentaDao,
            GestorPuntosService gestorPuntosService) {
        return new TransaccionRetiroCuentaService(cuentaDao, gestorPuntosService);
    }

    @Bean
    public TransferenciaService transferenciaService(
            CuentaDao cuentaDao,
            GestorPuntosService gestorPuntosService) {
        return new TransferenciaService(cuentaDao, gestorPuntosService);
    }

    @Bean
    public CrearCuentaService crearCuentaService(
            CuentaRepositorio cuentaRepositorio,
            ClienteDao clienteDao,
            CuentaDao cuentaDao) {
        return new CrearCuentaService(cuentaRepositorio, clienteDao, cuentaDao);
    }

    @Bean
    public BuscarClienteService buscarClienteService(ClienteDao clienteDao) {
        return new BuscarClienteService(clienteDao);
    }

    @Bean
    public CrearClienteService crearClienteService(
            ClienteDao clienteDao,
            ClienteRepositorio clienteRepositorio) {
        return new CrearClienteService(clienteDao, clienteRepositorio);
    }

    @Bean
    public BuscarCuentaService buscarCuentaService(CuentaDao cuentaDao) {
        return new BuscarCuentaService(cuentaDao);
    }

    @Bean
    public ColaPrioridadTransacciones colaPrioridadTransacciones() {
        return new ColaPrioridadTransacciones();
    }

    @Bean
    public GestorTransaccionesProgramadasService gestorTransaccionesProgramadasService(
            TransaccionProgramadaRepositorio repositorio,
            TransaccionDepositoCuentaService depositoService,
            TransaccionRetiroCuentaService retiroService,
            TransferenciaService transferenciaService,
            CuentaDao cuentaDao) {
        return new GestorTransaccionesProgramadasService(
            repositorio,
            depositoService,
            retiroService,
            transferenciaService,
            cuentaDao
        );
    }
}
