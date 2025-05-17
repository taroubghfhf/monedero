package co.edu.uniquindio.monedero.aplicacion.manejador.transaccion;

import co.edu.uniquindio.monedero.aplicacion.comando.TransferenciaComando;
import co.edu.uniquindio.monedero.dominio.dto.transferencia.ResultadoTransferenciaDTO;
import co.edu.uniquindio.monedero.dominio.exception.TransferenciaInvalidaException;
import co.edu.uniquindio.monedero.dominio.servicios.cuenta.TransferenciaService;
import org.springframework.stereotype.Component;

@Component
public class ManejadorTransferencia {

    private final TransferenciaService transferenciaService;

    public ManejadorTransferencia(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    public ResultadoTransferenciaDTO ejecutar(TransferenciaComando comando) {
        validarComando(comando);

        try {
            transferenciaService.ejecutar(
                comando.getCedulaClienteOrigen(),
                comando.getCedulaClienteDestino(),
                comando.getMonto()
            );

            return new ResultadoTransferenciaDTO(
                true,
                "Transferencia realizada exitosamente",
                comando.getMonto(),
                comando.getCedulaClienteOrigen(),
                comando.getCedulaClienteDestino()
            );

        } catch (Exception e) {
            return new ResultadoTransferenciaDTO(
                false,
                "Error en la transferencia: " + e.getMessage(),
                comando.getMonto(),
                comando.getCedulaClienteOrigen(),
                comando.getCedulaClienteDestino()
            );
        }
    }

    private void validarComando(TransferenciaComando comando) {
        if (comando == null) {
            throw new TransferenciaInvalidaException("El comando de transferencia no puede ser nulo");
        }

        if (comando.getCedulaClienteOrigen() == null || comando.getCedulaClienteOrigen().trim().isEmpty()) {
            throw new TransferenciaInvalidaException("La cédula del cliente origen es requerida");
        }

        if (comando.getCedulaClienteDestino() == null || comando.getCedulaClienteDestino().trim().isEmpty()) {
            throw new TransferenciaInvalidaException("La cédula del cliente destino es requerida");
        }

        if (comando.getMonto() <= 0) {
            throw new TransferenciaInvalidaException("El monto debe ser mayor a cero");
        }
    }
} 