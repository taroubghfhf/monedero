package co.edu.uniquindio.monedero.dominio.modelo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Monedero {
    private String nombre;
    private double saldo;
    private List<TransaccionMonedero> transaccionesMonedero;
}
