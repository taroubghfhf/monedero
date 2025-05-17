package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;

@Getter
public enum TipoBeneficio {
    REDUCCION_COMISION(100, "Reducción del 10% en la comisión por transferencias", 10),
    SIN_CARGOS_RETIROS(500, "Un mes sin cargos por retiros", 30),
    BONO_SALDO(1000, "Bono de saldo de 50 unidades", 50.0);

    private final int puntosRequeridos;
    private final String descripcion;
    private final double valor;

    TipoBeneficio(int puntosRequeridos, String descripcion, double valor) {
        this.puntosRequeridos = puntosRequeridos;
        this.descripcion = descripcion;
        this.valor = valor;
    }

    public static TipoBeneficio obtenerPorPuntos(int puntos) {
        for (TipoBeneficio beneficio : values()) {
            if (beneficio.getPuntosRequeridos() == puntos) {
                return beneficio;
            }
        }
        return null;
    }
} 