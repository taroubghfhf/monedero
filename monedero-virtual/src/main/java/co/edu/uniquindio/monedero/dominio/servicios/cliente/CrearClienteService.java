package co.edu.uniquindio.monedero.dominio.servicios.cliente;

import co.edu.uniquindio.monedero.dominio.exception.ClienteYaExisteException;
import co.edu.uniquindio.monedero.dominio.modelo.Cliente;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.dao.ClienteDao;
import co.edu.uniquindio.monedero.dominio.puerto.cliente.repositorio.ClienteRepositorio;
import co.edu.uniquindio.monedero.infraestructura.notificaciones.ServicioAlertas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrearClienteService {

    private final ClienteDao clienteDao;
    private final ClienteRepositorio clienteRepositorio;
    private final ServicioAlertas servicioAlertas;

    public boolean crearCliente(Cliente cliente) {
        try {
            if (clienteDao.buscarPorCedula(cliente.getCedula()) != null) {
                throw new ClienteYaExisteException("El cliente ya existe con la cédula: " + cliente.getCedula());
            }

            boolean clienteCreado = clienteRepositorio.agregarCliente(cliente);
            
            if (clienteCreado) {
                log.info("Cliente creado exitosamente: {} - {}", cliente.getCedula(), cliente.getNombre());

                try {
                    servicioAlertas.enviarNotificacionBienvenida(cliente);
                    log.info("Notificación de bienvenida programada para: {} ({})", 
                            cliente.getNombre(), cliente.getCorreo());
                } catch (Exception e) {
                    log.warn("No se pudo enviar notificación de bienvenida al cliente {}: {}", 
                            cliente.getCedula(), e.getMessage());
                }
            }
            
            return clienteCreado;
            
        } catch (ClienteYaExisteException e) {
            log.warn("Intento de crear cliente duplicado: {}", cliente.getCedula());
            throw e;
        } catch (Exception e) {
            log.error("Error al crear cliente {}: {}", cliente.getCedula(), e.getMessage(), e);
            throw new RuntimeException("Error interno al crear cliente", e);
        }
    }
}
