package co.edu.uniquindio.monedero.infraestructura.configuracion;

import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio.ClienteRepositorio;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.dao.CuentaDao;
import co.edu.uniquindio.monedero.dominio.puerto.cuenta.repositorio.CuentaRepositorio;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.BuscarClienteService;
import co.edu.uniquindio.monedero.dominio.servicios.cliente.CrearClienteService;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.CrearCuentaService;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.TransaccionDepositoCuentaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanServicio {

    @Bean
    public CrearCuentaService crearCuentaService(CuentaRepositorio cuentaRepositorio, ClienteDao clienteDao, CuentaDao cuentaDao) {
        return new CrearCuentaService(cuentaRepositorio, clienteDao, cuentaDao);
    }

    @Bean
    public BuscarClienteService buscarClienteService(ClienteDao clienteDao) {
        return new BuscarClienteService(clienteDao);
    }

    @Bean
    public CrearClienteService crearClienteService(ClienteDao clienteDao, ClienteRepositorio clienteRepositorio) {
        return new CrearClienteService(clienteDao, clienteRepositorio);
    }

    @Bean
    public TransaccionDepositoCuentaService transaccionDepositoCuentaService(CuentaDao cuentaDao) {
        return new TransaccionDepositoCuentaService(cuentaDao);
    }

}
