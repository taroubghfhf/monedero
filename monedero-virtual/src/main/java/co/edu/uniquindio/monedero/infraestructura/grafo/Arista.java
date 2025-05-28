package co.edu.uniquindio.monedero.infraestructura.grafo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Arista<T> {
    private T destino;
    private double peso;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Arista<?> arista = (Arista<?>) obj;
        return Double.compare(arista.peso, peso) == 0 && 
               destino != null ? destino.equals(arista.destino) : arista.destino == null;
    }
    
    @Override
    public int hashCode() {
        int result = destino != null ? destino.hashCode() : 0;
        long temp = Double.doubleToLongBits(peso);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    
    @Override
    public String toString() {
        return "Arista{" +
                "destino=" + destino +
                ", peso=" + peso +
                '}';
    }
} 