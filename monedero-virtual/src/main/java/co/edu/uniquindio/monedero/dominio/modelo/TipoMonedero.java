package co.edu.uniquindio.monedero.dominio.modelo;

public enum TipoMonedero {
    AHORROS("Ahorros", "Monedero destinado para ahorros"),
    GASTOS_DIARIOS("Gastos Diarios", "Monedero para gastos del día a día"),
    EMERGENCIAS("Emergencias", "Monedero para situaciones de emergencia"),
    VACACIONES("Vacaciones", "Monedero para ahorrar para vacaciones"),
    INVERSIONES("Inversiones", "Monedero para fondos de inversión"),
    PRINCIPAL("Principal", "Monedero principal de la cuenta");
    
    private final String nombre;
    private final String descripcion;
    
    TipoMonedero(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
} 