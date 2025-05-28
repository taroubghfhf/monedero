# Sistema de Alertas y Notificaciones - Monedero Virtual Uniquindío

## 📧 Descripción General

El sistema de alertas y notificaciones permite enviar automáticamente emails a los clientes usando Gmail SMTP, con gestión eficiente mediante listas circulares y procesamiento asíncrono.

## 🚀 Características Principales

### ✅ Funcionalidades Implementadas

1. **Notificación de Bienvenida Automática**
   - Se envía automáticamente cuando se crea un nuevo cliente
   - Template HTML personalizado con diseño moderno
   - Información sobre características del monedero

2. **Alertas de Saldo Bajo**
   - Detección automática de saldos por debajo del umbral configurado
   - Revisión cada hora (configurable)
   - Cache para evitar alertas duplicadas

3. **Recordatorios de Transacciones Programadas**
   - Notificaciones 24 horas antes de la ejecución
   - Revisión cada 2 horas (configurable)
   - Detalles completos de la transacción

4. **Lista Circular Eficiente**
   - Buffer circular de 100 notificaciones
   - Control de concurrencia con ReadWriteLock
   - Gestión por prioridades (CRITICA, ALTA, MEDIA, BAJA)
   - Procesamiento automático cada 30 segundos

5. **Templates HTML Profesionales**
   - Diseño responsive y moderno
   - Variables dinámicas con Thymeleaf
   - Fallback en caso de errores de template

## ⚙️ Configuración

### 🔧 Gmail SMTP

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME:tu-email@gmail.com}
    password: ${GMAIL_PASSWORD:tu-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### 📝 Variables de Entorno Requeridas

```bash
# Configurar en tu entorno o application.yaml
GMAIL_USERNAME=tu-email@gmail.com
GMAIL_PASSWORD=tu-app-password-generado
```

> **⚠️ Importante**: Para Gmail, debes generar una "Contraseña de Aplicación" en tu cuenta de Google, no usar tu contraseña personal.

### 🎛️ Configuración de Alertas

```yaml
notificaciones:
  alertas:
    saldo-bajo:
      habilitado: true
      umbral: 10000 # pesos colombianos
      frecuencia-revision: 3600 # segundos (1 hora)
    
    transacciones-programadas:
      habilitado: true
      recordatorio-anticipado: 86400 # 24 horas antes
      frecuencia-revision: 7200 # 2 horas
```

## 🏗️ Arquitectura del Sistema

### 📦 Componentes Principales

1. **NotificacionDTO**: DTO con tipos, prioridades y estados
2. **ListaCircularNotificaciones**: Buffer circular thread-safe
3. **ServicioEmail**: Envío de emails con templates Thymeleaf
4. **GestorNotificaciones**: Coordinador principal con procesamiento asíncrono
5. **ServicioAlertas**: Detección automática de condiciones de alerta

### 🔄 Flujo de Procesamiento

```
Cliente Creado → ServicioAlertas.enviarNotificacionBienvenida() 
                ↓
             GestorNotificaciones.enviarNotificacionInmediata()
                ↓
             ServicioEmail.enviarNotificacion()
                ↓
             Gmail SMTP → Email al Cliente
```

### 📊 Gestión de Prioridades

- **CRITICA (4)**: Procesada inmediatamente
- **ALTA (3)**: Procesada en segundo lugar
- **MEDIA (2)**: Procesada normalmente
- **BAJA (1)**: Procesada al final

## 🔌 API Endpoints

### 📈 Estadísticas del Sistema

```http
GET /api/notificaciones/estadisticas
```

**Respuesta:**
```json
{
  "gestor": {
    "notificacionesPendientes": 5,
    "capacidadTotal": 100,
    "porcentajeUso": 5.0,
    "intervaloProcesamiento": 30
  },
  "alertas": {
    "alertasEnCache": 12,
    "saldoBajoHabilitado": true,
    "transaccionesProgramadasHabilitado": true,
    "umbralSaldoBajo": 10000.0
  },
  "timestamp": 1703123456789
}
```

### 🧹 Limpiar Notificaciones

```http
DELETE /api/notificaciones/limpiar
```

### 💚 Estado de Salud

```http
GET /api/notificaciones/salud
```

## 📊 Dashboard Web

### 🌐 Interfaz de Monitoreo

El sistema incluye una interfaz web moderna para monitorear las estadísticas en tiempo real:

**URL**: `http://localhost:8080/notificaciones-dashboard.html`

### ✨ Características del Dashboard

- **📈 Visualización en Tiempo Real**: Actualización automática cada 30 segundos
- **🎨 Diseño Moderno**: Interfaz responsive con gradientes y animaciones
- **📊 Métricas Clave**:
  - Notificaciones pendientes vs. capacidad total
  - Porcentaje de uso del sistema (con indicadores de color)
  - Estado de alertas (habilitado/deshabilitado)
  - Alertas en cache
  - Umbral de saldo bajo configurado
- **⚡ Acciones Rápidas**:
  - Actualizar estadísticas manualmente
  - Verificar salud del sistema
  - Limpiar notificaciones pendientes
  - Navegación al inicio

### 🎯 Indicadores Visuales

- **🟢 Verde**: Sistema funcionando normal (uso < 60%)
- **🟡 Amarillo**: Advertencia (uso 60-80%)
- **🔴 Rojo**: Crítico (uso > 80%)
- **⚫ Gris**: Sistema deshabilitado

### 📱 Acceso desde el Menú Principal

El dashboard está integrado en la navegación principal de la aplicación web:
- Icono: 🔔 Notificaciones
- Se abre en una nueva pestaña para no interferir con el flujo principal

## 📧 Templates de Email

### 📁 Ubicación
```
src/main/resources/templates/email/
├── bienvenida.html
├── alerta-saldo-bajo.html
└── recordatorio-transaccion.html
```

### 🔧 Variables Disponibles

#### Bienvenida
- `${nombreCliente}`: Nombre del cliente
- `${emailCliente}`: Email del cliente
- `${año}`: Año actual
- `${nombreApp}`: Nombre de la aplicación

#### Alerta Saldo Bajo
- `${nombreCliente}`: Nombre del cliente
- `${saldoActual}`: Saldo actual formateado
- `${umbral}`: Umbral configurado
- `${fechaAlerta}`: Fecha de la alerta

#### Recordatorio Transacción
- `${nombreCliente}`: Nombre del cliente
- `${tipoTransaccion}`: Tipo de transacción
- `${monto}`: Monto formateado
- `${fechaEjecucion}`: Fecha de ejecución
- `${horaEjecucion}`: Hora de ejecución

## 🧪 Pruebas

### ✅ Probar Notificación de Bienvenida

1. Crear un nuevo cliente con email válido:
```http
POST /api/clientes
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Pérez",
  "cedula": "12345678",
  "correo": "juan.perez@ejemplo.com",
  "telefono": "3001234567"
}
```

2. Verificar en los logs que se envió la notificación:
```
INFO - Cliente creado exitosamente: 12345678 - Juan
INFO - Notificación de bienvenida programada para: Juan (juan.perez@ejemplo.com)
INFO - Notificación inmediata ... enviada exitosamente
```

### 📊 Monitorear el Sistema

```bash
# Ver estadísticas
curl http://localhost:8080/api/notificaciones/estadisticas

# Verificar salud
curl http://localhost:8080/api/notificaciones/salud
```

## 🐛 Solución de Problemas

### ❌ Problemas Comunes

1. **Error de autenticación Gmail**
   - Verificar que uses App Password, no contraseña personal
   - Habilitar 2FA en Google Account
   - Generar nueva App Password en Google Account Settings

2. **Notificaciones no se envían**
   - Verificar configuración SMTP en application.yaml
   - Revisar logs de `org.springframework.mail: DEBUG`
   - Verificar que `@EnableAsync` y `@EnableScheduling` estén configurados

3. **Templates no se procesan**
   - Verificar que los archivos HTML estén en `src/main/resources/templates/email/`
   - Revisar sintaxis Thymeleaf
   - Verificar variables en el contexto

### 📝 Logs Importantes

```yaml
logging:
  level:
    co.edu.uniquindio.monedero.infraestructura.notificaciones: DEBUG
    org.springframework.mail: DEBUG
```

## 🔮 Funcionalidades Futuras

- [ ] Notificaciones push móviles
- [ ] SMS con Twilio
- [ ] Dashboard web para gestión
- [ ] Métricas de entrega
- [ ] A/B testing de templates
- [ ] Integración con sistemas externos

## 📞 Soporte

Para problemas o preguntas sobre el sistema de notificaciones:

- **Email**: soporte@uniquindio.edu.co
- **Teléfono**: (6) 7359300 ext. 1234
- **Documentación**: Ver código en `src/main/java/.../infraestructura/notificaciones/`

---

> **🎓 Proyecto Académico**: Monedero Virtual - Universidad del Quindío, Facultad de Ingeniería 