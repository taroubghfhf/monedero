package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BeneficioActivo {
    private String cedulaCliente;
    private TipoBeneficio tipoBeneficio;
    private LocalDateTime fechaActivacion;
    private LocalDateTime fechaVencimiento;
    private boolean activo;

    public BeneficioActivo(String cedulaCliente, TipoBeneficio tipoBeneficio) {
        this.cedulaCliente = cedulaCliente;
        this.tipoBeneficio = tipoBeneficio;
        this.fechaActivacion = LocalDateTime.now();
        this.fechaVencimiento = calcularFechaVencimiento();
        this.activo = true;
    }

    private LocalDateTime calcularFechaVencimiento() {
        return switch (tipoBeneficio) {
            case SIN_CARGOS_RETIROS -> fechaActivacion.plusDays(30);
            case REDUCCION_COMISION -> fechaActivacion.plusDays(15);
            case BONO_SALDO -> fechaActivacion.plusDays(1);
        };
    }

    public boolean estaVigente() {
        return activo && LocalDateTime.now().isBefore(fechaVencimiento);
    }
} 