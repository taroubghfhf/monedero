package co.edu.uniquindio.monedero.dominio.modelo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Monedero {
    private String id;
    private String nombre;
    private TipoMonedero tipo;
    private double saldo;
    private String cuentaPropietaria;
    private List<TransaccionMonedero> transaccionesMonedero;
    
    public Monedero(String id, String nombre, TipoMonedero tipo, String cuentaPropietaria) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = 0.0;
        this.cuentaPropietaria = cuentaPropietaria;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Monedero monedero = (Monedero) obj;
        return Objects.equals(id, monedero.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Monedero{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", saldo=" + saldo +
                '}';
    }
}
