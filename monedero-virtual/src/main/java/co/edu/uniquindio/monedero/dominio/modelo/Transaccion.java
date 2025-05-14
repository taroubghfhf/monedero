package co.edu.uniquindio.monedero.dominio.modelo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {
    private double monto;
    private String id;
    private LocalDateTime fecha;
}
