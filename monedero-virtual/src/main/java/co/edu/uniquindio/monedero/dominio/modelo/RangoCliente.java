package co.edu.uniquindio.monedero.dominio.modelo;

import lombok.Getter;

@Getter
public enum RangoCliente {
    BRONCE(0, 500),
    PLATA(501, 1000),
    ORO(1001, 5000),
    PLATINO(5001, Integer.MAX_VALUE);

    private final int puntosMinimos;
    private final int puntosMaximos;

    RangoCliente(int puntosMinimos, int puntosMaximos) {
        this.puntosMinimos = puntosMinimos;
        this.puntosMaximos = puntosMaximos;
    }

    public static RangoCliente obtenerRangoPorPuntos(int puntos) {
        if (puntos >= PLATINO.puntosMinimos) {
            return PLATINO;
        } else if (puntos >= ORO.puntosMinimos) {
            return ORO;
        } else if (puntos >= PLATA.puntosMinimos) {
            return PLATA;
        }
        return BRONCE;
    }

    public String obtenerDescripcionBeneficios() {
        return switch (this) {
            case BRONCE -> "Sin descuentos en transferencias, puntos base x1.0";
            case PLATA -> "10% descuento en transferencias, puntos base x1.2";
            case ORO -> "20% descuento en transferencias, puntos base x1.5";
            case PLATINO -> "30% descuento en transferencias, puntos base x2.0";
        };
    }

    public int calcularPuntosParaSiguienteRango(int puntosActuales) {
        return switch (this) {
            case BRONCE -> PLATA.puntosMinimos - puntosActuales;
            case PLATA -> ORO.puntosMinimos - puntosActuales;
            case ORO -> PLATINO.puntosMinimos - puntosActuales;
            case PLATINO -> 0; // Ya está en el rango máximo
        };
    }

}