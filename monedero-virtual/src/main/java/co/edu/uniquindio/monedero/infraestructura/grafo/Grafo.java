package co.edu.uniquindio.monedero.infraestructura.grafo;

import java.util.*;

public class Grafo<T> {
    private Map<T, List<Arista<T>>> adyacencias;
    
    public Grafo() {
        this.adyacencias = new HashMap<>();
    }
    
    public void agregarVertice(T vertice) {
        adyacencias.putIfAbsent(vertice, new ArrayList<>());
    }
    
    public void agregarArista(T origen, T destino, double peso) {
        adyacencias.putIfAbsent(origen, new ArrayList<>());
        adyacencias.putIfAbsent(destino, new ArrayList<>());
        
        adyacencias.get(origen).add(new Arista<>(destino, peso));
    }
    
    public List<Arista<T>> obtenerAdyacentes(T vertice) {
        return adyacencias.getOrDefault(vertice, new ArrayList<>());
    }
    
    public Set<T> obtenerVertices() {
        return adyacencias.keySet();
    }
    
    public boolean existeVertice(T vertice) {
        return adyacencias.containsKey(vertice);
    }
    
    public boolean existeArista(T origen, T destino) {
        List<Arista<T>> adyacentes = adyacencias.get(origen);
        if (adyacentes == null) return false;
        
        return adyacentes.stream()
                .anyMatch(arista -> arista.getDestino().equals(destino));
    }
    
    public void eliminarVertice(T vertice) {
        adyacencias.remove(vertice);
        adyacencias.values().forEach(lista -> 
            lista.removeIf(arista -> arista.getDestino().equals(vertice))
        );
    }
    
    public void eliminarArista(T origen, T destino) {
        List<Arista<T>> adyacentes = adyacencias.get(origen);
        if (adyacentes != null) {
            adyacentes.removeIf(arista -> arista.getDestino().equals(destino));
        }
    }
    
    public List<T> obtenerCaminoMasCorto(T origen, T destino) {
        Map<T, T> padres = new HashMap<>();
        Map<T, Double> distancias = new HashMap<>();
        Set<T> visitados = new HashSet<>();
        
        // Inicializar distancias
        for (T vertice : adyacencias.keySet()) {
            distancias.put(vertice, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);
        
        PriorityQueue<T> cola = new PriorityQueue<>(
            Comparator.comparing(distancias::get)
        );
        cola.add(origen);
        
        while (!cola.isEmpty()) {
            T actual = cola.poll();
            
            if (visitados.contains(actual)) continue;
            visitados.add(actual);
            
            if (actual.equals(destino)) break;
            
            for (Arista<T> arista : obtenerAdyacentes(actual)) {
                T vecino = arista.getDestino();
                double nuevaDistancia = distancias.get(actual) + arista.getPeso();
                
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    padres.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        
        // Reconstruir camino
        List<T> camino = new ArrayList<>();
        T actual = destino;
        
        while (actual != null) {
            camino.add(0, actual);
            actual = padres.get(actual);
        }
        
        return camino.get(0).equals(origen) ? camino : new ArrayList<>();
    }
    
    public Map<T, List<Arista<T>>> obtenerMatrizAdyacencia() {
        return new HashMap<>(adyacencias);
    }
} 