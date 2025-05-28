// Elemento donde se cargará el contenido
const contentElement = document.getElementById('content');

// Función para actualizar el estado activo de la barra de navegación
function updateNavbarActiveState(module) {
  // Remover clase active de todos los enlaces
  document.querySelectorAll('.navbar-nav .nav-link').forEach(link => {
    link.classList.remove('active');
  });
  
  // Agregar clase active al enlace seleccionado
  const moduleMap = {
    'clientes': 0,
    'cuentas': 1,
    'monederos': 2,
    'transacciones': 3,
    'puntos': 4,
    'programadas': 5
  };
  
  const index = moduleMap[module];
  if (index !== undefined) {
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    if (navLinks[index]) {
      navLinks[index].classList.add('active');
    }
  }
}

// Función para mostrar mensajes
function showMessage(message, type = 'success') {
  const alertDiv = document.createElement('div');
  alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
  alertDiv.innerHTML = `
    ${message}
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
  `;
  contentElement.prepend(alertDiv);
  
  // Auto-ocultar después de 3 segundos
  setTimeout(() => {
    alertDiv.classList.remove('show');
    setTimeout(() => alertDiv.remove(), 150);
  }, 3000);
}

// Función para realizar peticiones HTTP
async function fetchAPI(url, options = {}) {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      }
    });
    
    if (!response.ok) {
      // Intenta obtener el error como JSON, si falla intenta obtener el texto
      let errorMessage = '';
      let exceptionName = '';
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || '';
        exceptionName = errorData.nombreExcepcion || '';
      } catch (e) {
        // Si falla al parsear JSON, intenta obtener el texto de la respuesta
        errorMessage = await response.text().catch(() => '');
      }
      
      // Mensajes personalizados por tipo de excepción
      if (exceptionName) {
        switch (exceptionName) {
          case 'ClienteYaExisteException':
            throw new Error(`Ya existe un cliente con esta cédula: ${errorMessage.split(': ')[1] || ''}`);
          case 'NoExisteClienteException':
            throw new Error(`No se encontró el cliente: ${errorMessage.split(': ')[1] || ''}`);
          case 'YaExisteCuentaException':
            throw new Error(`El cliente ya tiene una cuenta asociada.`);
          case 'CuentaNoExisteException':
            throw new Error(`La cuenta no existe: ${errorMessage.split(': ')[1] || ''}`);
          case 'SaldoInsuficienteException':
            throw new Error(`Saldo insuficiente para realizar la operación.`);
          case 'TransaccionInvalidaException':
            throw new Error(`Transacción inválida: ${errorMessage}`);
        }
      }
      
      // Personalizar mensajes de error según el código de estado
      let displayMessage = '';
      switch (response.status) {
        case 400:
          if (errorMessage.includes("cliente ya existe")) {
            displayMessage = `Ya existe un cliente con esta cédula: ${errorMessage.split(': ')[1] || ''}`;
          } else if (errorMessage.includes("no existe")) {
            displayMessage = `No existe: ${errorMessage}`;
          } else {
            displayMessage = `Solicitud incorrecta: ${errorMessage || 'Verifique los datos ingresados'}`;
          }
          break;
        case 401:
          displayMessage = 'No autorizado: Debe iniciar sesión nuevamente';
          break;
        case 403:
          displayMessage = 'Acceso denegado: No tiene permisos para esta operación';
          break;
        case 404:
          displayMessage = `No encontrado: ${errorMessage || 'El recurso solicitado no existe'}`;
          break;
        case 409:
          displayMessage = `Conflicto: ${errorMessage || 'La operación no puede completarse'}`;
          break;
        case 500:
          displayMessage = 'Error del servidor: Intente nuevamente más tarde';
          break;
        default:
          displayMessage = `Error HTTP ${response.status}: ${errorMessage || 'Algo salió mal'}`;
      }
      
      throw new Error(displayMessage);
    }
    
    // Para respuestas 204 No Content o respuestas vacías
    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
      return null;
    }
    
    // Verificar si hay contenido antes de intentar parsearlo como JSON
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  } catch (error) {
    // Solo mostrar el mensaje si no es un error de red (lo cual normalmente es manejado específicamente)
    if (!error.message.includes('Failed to fetch') && !error.message.includes('NetworkError')) {
      console.error('Error en fetchAPI:', error);
      showMessage(error.message, 'danger');
    }
    throw error;
  }
}

// ================ MÓDULO CLIENTES ================
function loadClientes() {
  updateNavbarActiveState('clientes');
  contentElement.innerHTML = `
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">Gestión de Clientes</h2>
      <img src="images/user-group.png" alt="Clientes" class="header-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/681/681494.png'; this.onerror='';">
    </div>
    
    <div class="row">
      <div class="col-lg-5">
        <div class="card client-card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Gestión de Clientes</h4>
            <i class="bi bi-people-fill"></i>
          </div>
          <div class="card-body">
            <ul class="nav nav-tabs mb-4" id="clientesTab" role="tablist">
              <li class="nav-item" role="presentation">
                <button class="nav-link active" id="lista-tab" data-bs-toggle="tab" data-bs-target="#lista" type="button" role="tab" aria-controls="lista" aria-selected="true">
                  <i class="bi bi-list-ul"></i> Listado
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="crear-tab" data-bs-toggle="tab" data-bs-target="#crear" type="button" role="tab" aria-controls="crear" aria-selected="false">
                  <i class="bi bi-person-plus"></i> Crear Nuevo
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="buscar-tab" data-bs-toggle="tab" data-bs-target="#buscar" type="button" role="tab" aria-controls="buscar" aria-selected="false">
                  <i class="bi bi-search"></i> Buscar
                </button>
              </li>
            </ul>
            
            <div class="tab-content" id="clientesTabContent">
              <!-- Tab de Listado -->
              <div class="tab-pane fade show active" id="lista" role="tabpanel" aria-labelledby="lista-tab">
                <div class="card">
                  <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">Listado de Clientes</h5>
                    <button class="btn btn-sm btn-light" onclick="fetchClientes()">
                      <i class="bi bi-arrow-clockwise"></i> Actualizar
                    </button>
                  </div>
                  <div class="card-body">
                    <div class="table-responsive">
                      <table class="table table-striped table-hover">
                        <thead class="table-light">
                          <tr>
                            <th>Cédula</th>
                            <th>Nombre</th>
                            <th>Apellido</th>
                            <th>Correo</th>
                            <th>Teléfono</th>
                            <th>Acciones</th>
                          </tr>
                        </thead>
                        <tbody id="clientesTableBody"></tbody>
                      </table>
                    </div>
                    <div id="clientesPlaceholder" class="text-center mt-4 d-none">
                      <img src="images/empty-list.png" alt="Sin clientes" style="max-width: 150px;" 
                          onerror="this.src='https://cdn-icons-png.flaticon.com/512/8922/8922621.png'; this.onerror='';">
                      <p class="mt-3 text-muted">No hay clientes registrados</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Crear Nuevo -->
              <div class="tab-pane fade" id="crear" role="tabpanel" aria-labelledby="crear-tab">
                <div class="row">
                  <div class="col-md-12">
                    <div class="form-container">
                      <div class="d-flex align-items-center mb-3">
                        <img src="images/add-user.png" alt="Crear cliente" class="icon-md me-2" 
                            onerror="this.src='https://cdn-icons-png.flaticon.com/512/8922/8922440.png'; this.onerror='';">
                        <h4 class="mb-0">Crear Nuevo Cliente</h4>
                      </div>
                      <form id="clienteForm">
                        <div class="row">
                          <div class="col-md-6 mb-3">
                            <label for="cedula" class="form-label">Cédula</label>
                            <input type="text" class="form-control" id="cedula" required>
                          </div>
                          <div class="col-md-6 mb-3">
                            <label for="telefono" class="form-label">Teléfono</label>
                            <input type="text" class="form-control" id="telefono" required>
                          </div>
                        </div>
                        <div class="row">
                          <div class="col-md-6 mb-3">
                            <label for="nombre" class="form-label">Nombre</label>
                            <input type="text" class="form-control" id="nombre" required>
                          </div>
                          <div class="col-md-6 mb-3">
                            <label for="apellido" class="form-label">Apellido</label>
                            <input type="text" class="form-control" id="apellido" required>
                          </div>
                        </div>
                        <div class="mb-3">
                          <label for="correo" class="form-label">Correo Electrónico</label>
                          <input type="email" class="form-control" id="correo" required>
                        </div>
                        <div class="text-end">
                          <button type="submit" class="btn btn-primary">
                            <i class="bi bi-person-plus"></i> Crear Cliente
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Buscar -->
              <div class="tab-pane fade" id="buscar" role="tabpanel" aria-labelledby="buscar-tab">
                <div class="row">
                  <div class="col-md-12">
                    <div class="form-container">
                      <div class="d-flex align-items-center mb-3">
                        <img src="images/search-user.png" alt="Buscar cliente" class="icon-md me-2" 
                            onerror="this.src='https://cdn-icons-png.flaticon.com/512/3126/3126554.png'; this.onerror='';">
                        <h4 class="mb-0">Buscar Cliente</h4>
                      </div>
                      <form id="buscarClienteForm">
                        <div class="input-group mb-3">
                          <input type="text" class="form-control" id="documentoBuscar" placeholder="Ingrese la cédula del cliente" required>
                          <button type="submit" class="btn btn-primary">
                            <i class="bi bi-search"></i> Buscar
                          </button>
                        </div>
                      </form>
                      <div id="clienteDetalles" class="mt-3"></div>
                      <div class="card card-info d-none" id="cardInfoBusqueda">
                        <div class="card-body">
                          <div class="d-flex align-items-center">
                            <img src="images/info.png" alt="Información" class="icon-md me-3" 
                                onerror="this.src='https://cdn-icons-png.flaticon.com/512/189/189664.png'; this.onerror='';">
                            <div>
                              <h5>Resultados de búsqueda</h5>
                              <p class="mb-0">Aquí aparecerá la información detallada del cliente que busque.</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-7">
        <div class="card client-card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Información</h4>
            <i class="bi bi-info-circle"></i>
          </div>
          <div class="card-body d-flex flex-column align-items-center justify-content-center">
            <img src="images/registro.png" alt="Registro de cliente" style="max-width: 50%;" 
                onerror="this.src='https://cdn-icons-png.flaticon.com/512/8922/8922948.png'; this.onerror='';">
            <h5 class="mt-4 text-center">Sistema de Gestión de Clientes</h5>
            <p class="text-muted text-center">Administre la información de sus clientes y gestione sus cuentas desde este módulo</p>
            <div class="card bg-light mt-3 w-100">
              <div class="card-body">
                <h6 class="mb-2">Funciones principales:</h6>
                <ul class="mb-0">
                  <li>Consulta de clientes registrados</li>
                  <li>Creación de nuevos clientes</li>
                  <li>Búsqueda por número de cédula</li>
                  <li>Acceso a la gestión de cuentas</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Cargar la lista de clientes
  fetchClientes();
  
  // Configurar evento para crear cliente
  document.getElementById('clienteForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const clienteData = {
      cedula: document.getElementById('cedula').value,
      nombre: document.getElementById('nombre').value,
      apellido: document.getElementById('apellido').value,
      correo: document.getElementById('correo').value,
      telefono: document.getElementById('telefono').value
    };
    
    try {
      // Mostrar indicador de carga
      const btnSubmit = e.target.querySelector('button[type="submit"]');
      const btnText = btnSubmit.innerHTML;
      btnSubmit.disabled = true;
      btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creando cliente...`;
      
      console.log('Enviando datos:', clienteData);
      
      // Usar fetchAPI en lugar de fetch directo
      await fetchAPI('/api/clientes', {
        method: 'POST',
        body: JSON.stringify(clienteData)
      });
      
      showMessage('Cliente creado exitosamente', 'success');
      document.getElementById('clienteForm').reset();
      fetchClientes();
    } catch (error) {
      console.error('Error al crear cliente:', error);
      
      // Mostrar error específico para cliente ya existente
      if (error.message.includes('Ya existe un cliente con esta cédula')) {
        const cedula = clienteData.cedula;
        const errorContainer = document.createElement('div');
        errorContainer.innerHTML = `
          <div class="alert alert-warning">
            <div class="d-flex align-items-center">
              <i class="bi bi-exclamation-triangle-fill fs-4 me-2"></i>
              <div>
                <h5 class="mb-1">Cliente ya registrado</h5>
                <p class="mb-0">El cliente con cédula <strong>${cedula}</strong> ya se encuentra registrado en el sistema.</p>
                <p class="mb-0 mt-2">
                  <button class="btn btn-sm btn-primary" onclick="verCliente('${cedula}')">
                    <i class="bi bi-eye"></i> Ver detalles del cliente
                  </button>
                </p>
              </div>
            </div>
          </div>
        `;
        
        // Mostrar el mensaje de error en el formulario
        const formContainer = document.querySelector('#crear .form-container');
        formContainer.insertBefore(errorContainer, formContainer.firstChild);
        
        // Remover el mensaje después de un tiempo
        setTimeout(() => {
          errorContainer.remove();
        }, 8000);
      }
    } finally {
      // Restaurar el botón
      const btnSubmit = e.target.querySelector('button[type="submit"]');
      btnSubmit.disabled = false;
      btnSubmit.innerHTML = `<i class="bi bi-person-plus"></i> Crear Cliente`;
    }
  });
  
  // Configurar evento para buscar cliente
  document.getElementById('buscarClienteForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedula = document.getElementById('documentoBuscar').value;
    const clienteDetalles = document.getElementById('clienteDetalles');
    const cardInfoBusqueda = document.getElementById('cardInfoBusqueda');
    
    // Mostrar indicador de carga
    clienteDetalles.innerHTML = `
      <div class="text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
        <p class="mt-2">Buscando cliente...</p>
      </div>
    `;
    
    try {
      const cliente = await fetchAPI(`/api/clientes/document/${cedula}`);
      
      // Ocultar la tarjeta de información y mostrar los resultados
      cardInfoBusqueda.classList.add('d-none');
      
      clienteDetalles.innerHTML = `
        <div class="card border-primary">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Información del Cliente</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-2 text-center">
                <img src="images/user-profile.png" alt="Perfil" class="img-fluid rounded-circle mb-3" 
                    style="max-width: 100px;" onerror="this.src='https://cdn-icons-png.flaticon.com/512/149/149071.png'; this.onerror='';">
              </div>
              <div class="col-md-10">
                <h4>${cliente.nombre} ${cliente.apellido}</h4>
                <div class="row mt-4">
                  <div class="col-md-6">
                    <p><strong>Cédula:</strong> <span class="badge bg-secondary">${cliente.cedula}</span></p>
                    <p><strong>Correo:</strong> <a href="mailto:${cliente.correo}">${cliente.correo}</a></p>
                  </div>
                  <div class="col-md-6">
                    <p><strong>Teléfono:</strong> <a href="tel:${cliente.telefono}">${cliente.telefono}</a></p>
                    <p>
                      <button class="btn btn-sm btn-primary" onclick="seleccionarClienteParaCuenta('${cliente.cedula}')">
                        <i class="bi bi-wallet2"></i> Gestionar Cuenta
                      </button>
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      `;
    } catch (error) {
      clienteDetalles.innerHTML = `
        <div class="alert alert-danger">
          <h5><i class="bi bi-exclamation-triangle-fill"></i> Cliente no encontrado</h5>
          <p>No se encontró ningún cliente con la cédula ${cedula}.</p>
        </div>
      `;
      
      // Mostrar la tarjeta de información
      cardInfoBusqueda.classList.remove('d-none');
      
      console.error('Error al buscar cliente:', error);
    }
  });
}

async function fetchClientes() {
  try {
    const clientes = await fetchAPI('/api/clientes');
    const tableBody = document.getElementById('clientesTableBody');
    const placeholder = document.getElementById('clientesPlaceholder');
    
    tableBody.innerHTML = '';
    
    if (clientes && clientes.length > 0) {
      placeholder.classList.add('d-none');
      
      clientes.forEach(cliente => {
        const row = document.createElement('tr');
        row.innerHTML = `
          <td><span class="badge bg-secondary">${cliente.cedula}</span></td>
          <td>${cliente.nombre}</td>
          <td>${cliente.apellido}</td>
          <td><a href="mailto:${cliente.correo}">${cliente.correo}</a></td>
          <td><a href="tel:${cliente.telefono}">${cliente.telefono}</a></td>
          <td>
            <div class="btn-group btn-group-sm" role="group">
              <button class="btn btn-info" onclick="verCliente('${cliente.cedula}')">
                <i class="bi bi-eye"></i>
              </button>
              <button class="btn btn-outline-primary" onclick="seleccionarClienteParaCuenta('${cliente.cedula}')">
                <i class="bi bi-wallet2"></i>
              </button>
            </div>
          </td>
        `;
        tableBody.appendChild(row);
      });
    } else {
      placeholder.classList.remove('d-none');
    }
  } catch (error) {
    console.error('Error al cargar clientes:', error);
    document.getElementById('clientesPlaceholder').classList.remove('d-none');
  }
}

// Función auxiliar para seleccionar un cliente para crear una cuenta
function seleccionarClienteParaCuenta(cedula) {
  loadCuentas();
  setTimeout(() => {
    document.getElementById('cedulaCliente').value = cedula;
  }, 300);
}

function verCliente(cedula) {
  // Cambiar a la pestaña de búsqueda
  document.getElementById('buscar-tab').click();
  
  // Pequeña espera para asegurar que la pestaña se haya cargado
  setTimeout(() => {
    document.getElementById('documentoBuscar').value = cedula;
    document.getElementById('buscarClienteForm').dispatchEvent(new Event('submit'));
  }, 300);
}

// ================ MÓDULO CUENTAS ================
function loadCuentas() {
  updateNavbarActiveState('cuentas');
  contentElement.innerHTML = `
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">Gestión de Cuentas</h2>
      <img src="images/wallet.png" alt="Cuentas" class="header-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/2830/2830284.png'; this.onerror='';">
    </div>
    
    <div class="row">
      <div class="col-lg-5">
        <div class="card account-card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Operaciones de Cuenta</h4>
            <i class="bi bi-wallet-fill"></i>
          </div>
          <div class="card-body">
            <ul class="nav nav-tabs mb-4" id="cuentasTab" role="tablist">
              <li class="nav-item" role="presentation">
                <button class="nav-link active" id="crear-cuenta-tab" data-bs-toggle="tab" data-bs-target="#crear-cuenta" type="button" role="tab" aria-controls="crear-cuenta" aria-selected="true">
                  <i class="bi bi-wallet-fill"></i> Crear Cuenta
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="consultar-cuenta-tab" data-bs-toggle="tab" data-bs-target="#consultar-cuenta" type="button" role="tab" aria-controls="consultar-cuenta" aria-selected="false">
                  <i class="bi bi-search"></i> Consultar Cuenta
                </button>
              </li>
            </ul>
            
            <div class="tab-content" id="cuentasTabContent">
              <!-- Tab de Crear Cuenta -->
              <div class="tab-pane fade show active" id="crear-cuenta" role="tabpanel" aria-labelledby="crear-cuenta-tab">
                <div class="form-container">
                  <div class="d-flex align-items-center mb-3">
                    <img src="images/add-wallet.png" alt="Crear cuenta" class="icon-md me-2" 
                        onerror="this.src='https://cdn-icons-png.flaticon.com/512/2257/2257295.png'; this.onerror='';">
                    <h4 class="mb-0">Crear Nueva Cuenta</h4>
                  </div>
                  <form id="cuentaForm">
                    <div class="mb-3">
                      <label for="cedulaCliente" class="form-label">Cédula del Cliente</label>
                      <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                        <input type="text" class="form-control" id="cedulaCliente" required 
                          placeholder="Ingrese la cédula del cliente">
                        <button type="button" class="btn btn-outline-secondary" onclick="buscarClienteParaCuenta()">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                      <small class="text-muted">El cliente debe estar registrado en el sistema</small>
                    </div>
                    <div class="text-end">
                      <button type="submit" class="btn btn-primary">
                        <i class="bi bi-plus-circle"></i> Crear Cuenta
                      </button>
                    </div>
                  </form>
                  <div id="clienteSeleccionadoInfo" class="mt-3 d-none">
                    <div class="alert alert-info">
                      <h5><i class="bi bi-person-check"></i> Cliente Seleccionado</h5>
                      <div id="clienteSeleccionadoDetalles"></div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Consultar Cuenta -->
              <div class="tab-pane fade" id="consultar-cuenta" role="tabpanel" aria-labelledby="consultar-cuenta-tab">
                <div class="form-container">
                  <div class="d-flex align-items-center mb-3">
                    <img src="images/search-account.png" alt="Buscar cuenta" class="icon-md me-2" 
                        onerror="this.src='https://cdn-icons-png.flaticon.com/512/954/954591.png'; this.onerror='';">
                    <h4 class="mb-0">Consultar Cuenta</h4>
                  </div>
                  <form id="buscarCuentaForm">
                    <div class="input-group mb-3">
                      <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                      <input type="text" class="form-control" id="cedulaClienteBuscar" placeholder="Ingrese la cédula del cliente" required>
                      <button type="submit" class="btn btn-primary">
                        <i class="bi bi-search"></i> Buscar
                      </button>
                    </div>
                  </form>
                  <div id="cuentaDetalles" class="mt-3"></div>
                  <div class="card card-info d-none" id="cardInfoBusquedaCuenta">
                    <div class="card-body">
                      <div class="d-flex align-items-center">
                        <img src="images/info.png" alt="Información" class="icon-md me-3" 
                            onerror="this.src='https://cdn-icons-png.flaticon.com/512/189/189664.png'; this.onerror='';">
                        <div>
                          <h5>Resultados de búsqueda</h5>
                          <p class="mb-0">Aquí aparecerá la información detallada de la cuenta del cliente.</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-7">
        <div class="card account-card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Información de Cuentas</h4>
            <i class="bi bi-info-circle"></i>
          </div>
          <div class="card-body d-flex flex-column align-items-center justify-content-center">
            <img src="images/bank-account.png" alt="Cuenta bancaria" style="max-width: 50%;" 
                onerror="this.src='https://cdn-icons-png.flaticon.com/512/2331/2331941.png'; this.onerror='';">
            <h5 class="mt-4 text-center">Sistema de Cuentas Virtuales</h5>
            <p class="text-muted text-center">Las cuentas permiten a los clientes realizar transacciones y acumular puntos</p>
            <div class="card bg-light mt-3 w-100">
              <div class="card-body">
                <h6 class="mb-2">Características principales:</h6>
                <ul class="mb-0">
                  <li>Depósitos y retiros simplificados</li>
                  <li>Transferencias entre cuentas</li>
                  <li>Acumulación de puntos automática</li>
                  <li>Consulta de saldos en tiempo real</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Función para buscar cliente para cuenta
  window.buscarClienteParaCuenta = async function() {
    const cedula = document.getElementById('cedulaCliente').value;
    const clienteInfo = document.getElementById('clienteSeleccionadoInfo');
    const clienteDetalles = document.getElementById('clienteSeleccionadoDetalles');
    
    if (!cedula) {
      showMessage('Ingrese una cédula para buscar el cliente', 'warning');
      return;
    }
    
    clienteInfo.classList.add('d-none');
    
    try {
      const cliente = await fetchAPI(`/api/clientes/document/${cedula}`);
      
      clienteDetalles.innerHTML = `
        <div class="d-flex align-items-center">
          <div class="me-3">
            <img src="images/user-profile.png" alt="Perfil" class="rounded-circle" 
                style="width: 50px; height: 50px;" onerror="this.src='https://cdn-icons-png.flaticon.com/512/149/149071.png'; this.onerror='';">
          </div>
          <div>
            <p class="mb-1"><strong>${cliente.nombre} ${cliente.apellido}</strong></p>
            <p class="mb-1"><small>Cédula: ${cliente.cedula}</small></p>
            <p class="mb-0"><small>Correo: ${cliente.correo}</small></p>
          </div>
        </div>
      `;
      
      clienteInfo.classList.remove('d-none');
    } catch (error) {
      showMessage(`No se encontró ningún cliente con la cédula ${cedula}`, 'danger');
      console.error('Error al buscar cliente:', error);
    }
  }

  // Configurar evento para crear cuenta
  document.getElementById('cuentaForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaCliente').value;
    
    if (!cedulaCliente) {
      showMessage('Ingrese la cédula del cliente', 'warning');
      return;
    }
    
    // Verificar primero si el cliente existe
    try {
      // Primero verificamos que el cliente exista
      try {
        await fetchAPI(`/api/clientes/document/${cedulaCliente}`);
      } catch (clienteError) {
        showMessage(`El cliente con cédula ${cedulaCliente} no existe. Debe registrar el cliente primero.`, 'warning');
        return;
      }
      
      // Luego verificamos si ya tiene una cuenta
      try {
        await fetchAPI(`/api/cuenta/buscar/${cedulaCliente}`);
        showMessage(`El cliente con cédula ${cedulaCliente} ya tiene una cuenta asociada.`, 'warning');
        
        // Redirigir a la visualización de la cuenta
        setTimeout(() => {
          document.getElementById('consultar-cuenta-tab').click();
          document.getElementById('cedulaClienteBuscar').value = cedulaCliente;
          document.getElementById('buscarCuentaForm').dispatchEvent(new Event('submit'));
        }, 800);
        return;
      } catch (cuentaError) {
        // Si la cuenta no existe, es el comportamiento esperado, continuamos
      }
      
      // Mostrar indicador de carga
      const btnSubmit = e.target.querySelector('button[type="submit"]');
      const btnText = btnSubmit.innerHTML;
      btnSubmit.disabled = true;
      btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creando cuenta...`;
      
      const cuentaData = {
        cedulaCliente: cedulaCliente
      };
      
      const response = await fetch('/api/cuenta', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(cuentaData)
      });
      
      btnSubmit.disabled = false;
      btnSubmit.innerHTML = btnText;
      
      if (response.ok) {
        showMessage('Cuenta creada exitosamente', 'success');
        document.getElementById('cuentaForm').reset();
        document.getElementById('clienteSeleccionadoInfo').classList.add('d-none');
        
        // Cambiar a la pestaña de consulta y buscar la cuenta recién creada
        setTimeout(() => {
          document.getElementById('consultar-cuenta-tab').click();
          document.getElementById('cedulaClienteBuscar').value = cedulaCliente;
          document.getElementById('buscarCuentaForm').dispatchEvent(new Event('submit'));
        }, 800);
      } else {
        // Intentar obtener más detalles del error
        let errorMsg = `Error (${response.status})`;
        try {
          const errorData = await response.json();
          errorMsg = errorData.message || errorData.error || `Error del servidor: ${response.status}`;
        } catch (e) {
          // Si no podemos obtener un JSON, usamos el texto
          const errorText = await response.text();
          errorMsg = errorText || `Error del servidor: ${response.status}`;
        }
        
        if (response.status === 400) {
          if (errorMsg.includes("ya tiene una cuenta")) {
            showMessage(`El cliente ya tiene una cuenta asociada. No se puede crear otra.`, 'warning');
          } else if (errorMsg.includes("no existe")) {
            showMessage(`El cliente con cédula ${cedulaCliente} no está registrado en el sistema.`, 'warning');
          } else {
            showMessage(`No se pudo crear la cuenta: ${errorMsg}`, 'danger');
          }
        } else {
          showMessage(`No se pudo crear la cuenta: ${errorMsg}`, 'danger');
        }
        
        console.error('Error al crear cuenta:', errorMsg);
      }
    } catch (error) {
      console.error('Error al crear cuenta:', error);
      showMessage(`Error de conexión: ${error.message}. Intente nuevamente más tarde.`, 'danger');
    }
  });
  
  // Configurar evento para buscar cuenta
  document.getElementById('buscarCuentaForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaClienteBuscar').value;
    const cuentaDetalles = document.getElementById('cuentaDetalles');
    const cardInfoBusquedaCuenta = document.getElementById('cardInfoBusquedaCuenta');
    
    // Mostrar indicador de carga
    cuentaDetalles.innerHTML = `
      <div class="text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
        <p class="mt-2">Buscando cuenta...</p>
      </div>
    `;
    
    try {
      const cuenta = await fetchAPI(`/api/cuenta/buscar/${cedulaCliente}`);
      
      // Ocultar la tarjeta de información y mostrar los resultados
      cardInfoBusquedaCuenta.classList.add('d-none');
      
      cuentaDetalles.innerHTML = `
        <div class="card border-primary">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Información de la Cuenta</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-4 text-center">
                <div class="mb-3">
                  <img src="images/account-details.png" alt="Cuenta" style="max-width: 100px;" 
                      onerror="this.src='https://cdn-icons-png.flaticon.com/512/2474/2474451.png'; this.onerror='';">
                </div>
                <h3 class="text-primary mb-0">#${cuenta.numeroCuenta}</h3>
                <p class="text-muted">Número de cuenta</p>
              </div>
              <div class="col-md-8">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <div class="card bg-light">
                      <div class="card-body text-center">
                        <h6 class="card-subtitle mb-2 text-muted">Saldo Disponible</h6>
                        <h4 class="card-title text-success">$${cuenta.saldoCuenta.toFixed(2)}</h4>
                      </div>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="card bg-light">
                      <div class="card-body text-center">
                        <h6 class="card-subtitle mb-2 text-muted">Puntos Acumulados</h6>
                        <h4 class="card-title text-primary">${cuenta.totalPuntos} pts</h4>
                      </div>
                    </div>
                  </div>
                </div>
                <p><strong>Cliente:</strong> <span class="badge bg-secondary">${cuenta.numeroCliente}</span></p>
                <p><strong>Saldo Total:</strong> $${cuenta.saldoTotal.toFixed(2)}</p>
                <div class="mt-3">
                  <button class="btn btn-sm btn-success me-2" onclick="cargarTransaccionDeposito('${cuenta.numeroCliente}', '${cuenta.numeroCuenta}')">
                    <i class="bi bi-cash"></i> Depositar
                  </button>
                  <button class="btn btn-sm btn-warning me-2" onclick="cargarTransaccionRetiro('${cuenta.numeroCliente}', '${cuenta.numeroCuenta}')">
                    <i class="bi bi-cash-stack"></i> Retirar
                  </button>
                  <button class="btn btn-sm btn-info" onclick="verHistorialTransacciones('${cuenta.numeroCliente}')">
                    <i class="bi bi-clock-history"></i> Historial
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      `;
    } catch (error) {
      cuentaDetalles.innerHTML = `
        <div class="alert alert-danger">
          <h5><i class="bi bi-exclamation-triangle-fill"></i> Cuenta no encontrada</h5>
          <p>No se encontró ninguna cuenta asociada al cliente con cédula ${cedulaCliente}.</p>
          <button class="btn btn-sm btn-primary mt-2" onclick="crearCuentaParaCliente('${cedulaCliente}')">
            <i class="bi bi-plus-circle"></i> Crear cuenta para este cliente
          </button>
        </div>
      `;
      
      // Mostrar la tarjeta de información
      cardInfoBusquedaCuenta.classList.remove('d-none');
      
      console.error('Error al buscar cuenta:', error);
    }
  });
}

// Funciones de navegación para cuentas y transacciones
function crearCuentaParaCliente(cedula) {
  document.getElementById('crear-cuenta-tab').click();
  setTimeout(() => {
    document.getElementById('cedulaCliente').value = cedula;
    buscarClienteParaCuenta();
  }, 300);
}

function cargarTransaccionDeposito(cedulaCliente, numeroCuenta) {
  loadTransacciones();
  setTimeout(() => {
    document.getElementById('cedulaClienteDeposito').value = cedulaCliente;
    document.getElementById('numeroCuentaDeposito').value = numeroCuenta || '';
  }, 300);
}

function cargarTransaccionRetiro(cedulaCliente, numeroCuenta) {
  loadTransacciones();
  setTimeout(() => {
    document.getElementById('cedulaClienteRetiro').value = cedulaCliente;
    document.getElementById('numeroCuentaRetiro').value = numeroCuenta || '';
  }, 300);
}

function verHistorialTransacciones(cedulaCliente) {
  loadTransacciones();
  setTimeout(() => {
    // Cambiar a la pestaña de historial si existe
    const historicoTab = document.getElementById('historico-tab');
    if (historicoTab) historicoTab.click();
    
    // Establecer el valor en el formulario
    document.getElementById('cedulaClienteHistorico').value = cedulaCliente;
    document.getElementById('historicoForm').dispatchEvent(new Event('submit'));
  }, 300);
}

// ================ MÓDULO TRANSACCIONES ================
function loadTransacciones() {
  updateNavbarActiveState('transacciones');
  contentElement.innerHTML = `
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">Gestión de Transacciones</h2>
      <img src="images/transaction.png" alt="Transacciones" class="header-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/2645/2645233.png'; this.onerror='';">
    </div>
    
    <div class="row">
      <div class="col-lg-5">
        <div class="card transaction-card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center bg-success text-white">
            <h4 class="mb-0">Operaciones</h4>
            <i class="bi bi-arrow-left-right"></i>
          </div>
          <div class="card-body">
            <ul class="nav nav-tabs mb-4" id="transaccionesTab" role="tablist">
              <li class="nav-item" role="presentation">
                <button class="nav-link active" id="deposito-tab" data-bs-toggle="tab" data-bs-target="#deposito" type="button" role="tab" aria-controls="deposito" aria-selected="true">
                  <i class="bi bi-cash-coin"></i> Depósito
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="retiro-tab" data-bs-toggle="tab" data-bs-target="#retiro" type="button" role="tab" aria-controls="retiro" aria-selected="false">
                  <i class="bi bi-cash-stack"></i> Retiro
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="transferencia-tab" data-bs-toggle="tab" data-bs-target="#transferencia" type="button" role="tab" aria-controls="transferencia" aria-selected="false">
                  <i class="bi bi-arrow-left-right"></i> Transferencia
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="historico-tab" data-bs-toggle="tab" data-bs-target="#historico" type="button" role="tab" aria-controls="historico" aria-selected="false">
                  <i class="bi bi-clock-history"></i> Historial
                </button>
              </li>
            </ul>
            
            <div class="tab-content" id="transaccionesTabContent">
              <!-- Tab de Depósito -->
              <div class="tab-pane fade show active" id="deposito" role="tabpanel" aria-labelledby="deposito-tab">
                <div class="card transaction-inner-card mb-3">
                  <div class="card-header bg-success text-white d-flex align-items-center">
                    <img src="images/deposit.png" alt="Depósito" class="icon-md me-2 bg-white rounded p-1" 
                        onerror="this.src='https://cdn-icons-png.flaticon.com/512/2529/2529532.png'; this.onerror='';">
                    <h5 class="mb-0">Realizar Depósito</h5>
                  </div>
                  <div class="card-body">
                    <form id="depositoForm">
                      <div class="row">
                        <div class="col-md-6 mb-3">
                          <label for="cedulaClienteDeposito" class="form-label">Cédula del Cliente</label>
                          <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                            <input type="text" class="form-control" id="cedulaClienteDeposito" 
                              placeholder="Ingrese cédula" required>
                            <button type="button" class="btn btn-outline-success" onclick="buscarClienteParaTransaccion('cedulaClienteDeposito')">
                              <i class="bi bi-search"></i>
                            </button>
                          </div>
                        </div>
                        <div class="col-md-6 mb-3">
                          <label for="numeroCuentaDeposito" class="form-label">Número de Cuenta</label>
                          <input type="text" class="form-control" id="numeroCuentaDeposito" 
                            placeholder="Opcional - Se buscará por cédula">
                        </div>
                      </div>
                      <div class="mb-3">
                        <label for="montoDeposito" class="form-label">Monto a Depositar</label>
                        <div class="input-group">
                          <span class="input-group-text">$</span>
                          <input type="number" class="form-control" id="montoDeposito" required min="1" 
                            placeholder="Ingrese el monto a depositar">
                        </div>
                      </div>
                      <div class="text-end">
                        <button type="submit" class="btn btn-success">
                          <i class="bi bi-check-circle"></i> Realizar Depósito
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Retiro -->
              <div class="tab-pane fade" id="retiro" role="tabpanel" aria-labelledby="retiro-tab">
                <div class="card transaction-inner-card mb-3">
                  <div class="card-header bg-warning text-dark d-flex align-items-center">
                    <img src="images/withdraw.png" alt="Retiro" class="icon-md me-2 bg-white rounded p-1" 
                        onerror="this.src='https://cdn-icons-png.flaticon.com/512/5972/5972757.png'; this.onerror='';">
                    <h5 class="mb-0">Realizar Retiro</h5>
                  </div>
                  <div class="card-body">
                    <form id="retiroForm">
                      <div class="row">
                        <div class="col-md-6 mb-3">
                          <label for="cedulaClienteRetiro" class="form-label">Cédula del Cliente</label>
                          <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                            <input type="text" class="form-control" id="cedulaClienteRetiro" 
                              placeholder="Ingrese cédula" required>
                            <button type="button" class="btn btn-outline-warning" onclick="buscarClienteParaTransaccion('cedulaClienteRetiro')">
                              <i class="bi bi-search"></i>
                            </button>
                          </div>
                        </div>
                        <div class="col-md-6 mb-3">
                          <label for="numeroCuentaRetiro" class="form-label">Número de Cuenta</label>
                          <input type="text" class="form-control" id="numeroCuentaRetiro" 
                            placeholder="Opcional - Se buscará por cédula">
                        </div>
                      </div>
                      <div class="mb-3">
                        <label for="montoRetiro" class="form-label">Monto a Retirar</label>
                        <div class="input-group">
                          <span class="input-group-text">$</span>
                          <input type="number" class="form-control" id="montoRetiro" required min="1" 
                            placeholder="Ingrese el monto a retirar">
                        </div>
                      </div>
                      <div class="text-end">
                        <button type="submit" class="btn btn-warning">
                          <i class="bi bi-check-circle"></i> Realizar Retiro
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Transferencia -->
              <div class="tab-pane fade" id="transferencia" role="tabpanel" aria-labelledby="transferencia-tab">
                <div class="card transaction-inner-card mb-3">
                  <div class="card-header bg-primary text-white d-flex align-items-center">
                    <img src="images/transfer.png" alt="Transferencia" class="icon-md me-2 bg-white rounded p-1" 
                        onerror="this.src='https://cdn-icons-png.flaticon.com/512/5738/5738077.png'; this.onerror='';">
                    <h5 class="mb-0">Realizar Transferencia</h5>
                  </div>
                  <div class="card-body">
                    <form id="transferenciaForm">
                      <div class="row">
                        <div class="col-md-6">
                          <div class="card transfer-card mb-3">
                            <div class="card-header bg-danger text-white">
                              <h5 class="mb-0"><i class="bi bi-arrow-up-circle"></i> Cuenta Origen</h5>
                            </div>
                            <div class="card-body">
                              <div class="mb-3">
                                <label for="cedulaOrigen" class="form-label">Cédula Cliente Origen</label>
                                <div class="input-group">
                                  <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                                  <input type="text" class="form-control" id="cedulaOrigen" required 
                                    placeholder="Cédula del remitente">
                                  <button type="button" class="btn btn-outline-danger" onclick="buscarClienteParaTransferencia('origen')">
                                    <i class="bi bi-search"></i>
                                  </button>
                                </div>
                              </div>
                              <div id="infoClienteOrigen" class="d-none">
                                <div class="alert alert-danger border-0">
                                  <div id="detalleClienteOrigen"></div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                        <div class="col-md-6">
                          <div class="card transfer-card mb-3">
                            <div class="card-header bg-success text-white">
                              <h5 class="mb-0"><i class="bi bi-arrow-down-circle"></i> Cuenta Destino</h5>
                            </div>
                            <div class="card-body">
                              <div class="mb-3">
                                <label for="cedulaDestino" class="form-label">Cédula Cliente Destino</label>
                                <div class="input-group">
                                  <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                                  <input type="text" class="form-control" id="cedulaDestino" required 
                                    placeholder="Cédula del destinatario">
                                  <button type="button" class="btn btn-outline-success" onclick="buscarClienteParaTransferencia('destino')">
                                    <i class="bi bi-search"></i>
                                  </button>
                                </div>
                              </div>
                              <div id="infoClienteDestino" class="d-none">
                                <div class="alert alert-success border-0">
                                  <div id="detalleClienteDestino"></div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="mb-3">
                        <label for="montoTransferencia" class="form-label">Monto a Transferir</label>
                        <div class="input-group">
                          <span class="input-group-text">$</span>
                          <input type="number" class="form-control" id="montoTransferencia" required min="1" 
                            placeholder="Ingrese el monto a transferir">
                        </div>
                      </div>
                      <div class="mb-3">
                        <label for="descripcionTransferencia" class="form-label">Descripción (Opcional)</label>
                        <textarea class="form-control" id="descripcionTransferencia" rows="2" 
                          placeholder="Ingrese una descripción o motivo de la transferencia"></textarea>
                      </div>
                      <div class="text-end">
                        <button type="button" class="btn btn-outline-secondary me-2" onclick="limpiarFormularioTransferencia()">
                          <i class="bi bi-x-circle"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-primary">
                          <i class="bi bi-arrow-left-right"></i> Realizar Transferencia
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Historial -->
              <div class="tab-pane fade" id="historico" role="tabpanel" aria-labelledby="historico-tab">
                <div class="card transaction-inner-card mb-3">
                  <div class="card-header bg-info text-white d-flex align-items-center">
                    <img src="images/history.png" alt="Historial" class="icon-md me-2 bg-white rounded p-1" 
                        onerror="this.src='https://cdn-icons-png.flaticon.com/512/6497/6497464.png'; this.onerror='';">
                    <h5 class="mb-0">Historial de Transacciones</h5>
                  </div>
                  <div class="card-body">
                    <form id="historicoForm" class="mb-3">
                      <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                        <input type="text" class="form-control" id="cedulaClienteHistorico" 
                          placeholder="Ingrese la cédula del cliente" required>
                        <button type="submit" class="btn btn-info text-white">
                          <i class="bi bi-search"></i> Consultar
                        </button>
                      </div>
                    </form>
                    <div id="transaccionesHistorico"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-7">
        <div class="card transaction-card">
          <div class="card-header d-flex justify-content-between align-items-center bg-success text-white">
            <h4 class="mb-0">Información de Transacciones</h4>
            <i class="bi bi-info-circle"></i>
          </div>
          <div class="card-body d-flex flex-column align-items-center justify-content-center">
            <img src="images/money-transfer.png" alt="Transferencia" style="max-width: 50%;" 
                onerror="this.src='https://cdn-icons-png.flaticon.com/512/2968/2968268.png'; this.onerror='';">
            <h5 class="mt-4 text-center">Sistema de Transacciones</h5>
            <p class="text-muted text-center">Gestione depósitos, retiros y transferencias de manera segura y eficiente</p>
            <div class="card bg-light mt-3 w-100">
              <div class="card-body">
                <h6 class="mb-2">Beneficios:</h6>
                <ul class="mb-0">
                  <li>Transacciones instantáneas</li>
                  <li>Historial detallado de operaciones</li>
                  <li>Acumulación automática de puntos</li>
                  <li>Transferencias entre cuentas de clientes</li>
                </ul>
              </div>
            </div>
            <div class="card text-white bg-success mt-3 w-100">
              <div class="card-body">
                <div class="d-flex align-items-center">
                  <i class="bi bi-lightbulb-fill me-2 fs-4"></i>
                  <div>
                    <h6 class="card-title mb-1">¡Consejo!</h6>
                    <p class="card-text mb-0">Las transferencias generan más puntos que los depósitos y retiros.</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Configurar evento para depósito
  document.getElementById('depositoForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaClienteDeposito').value;
    const monto = parseFloat(document.getElementById('montoDeposito').value);
    const numeroCuenta = document.getElementById('numeroCuentaDeposito').value;
    
    if (!cedulaCliente) {
      showMessage('Ingrese la cédula del cliente', 'warning');
      return;
    }
    
    if (!monto || monto <= 0) {
      showMessage('Ingrese un monto válido mayor a cero', 'warning');
      return;
    }
    
    // Mostrar indicador de carga y deshabilitar botón
    const btnSubmit = e.target.querySelector('button[type="submit"]');
    const btnText = btnSubmit.innerHTML;
    btnSubmit.disabled = true;
    btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...`;
    
    // Crear div de animación
    const animacionDiv = document.createElement('div');
    animacionDiv.className = 'text-center my-4 transaction-animation';
    animacionDiv.innerHTML = `
      <div class="transaction-progress">
        <div class="spinner-grow text-primary mx-2" role="status"></div>
        <div class="spinner-grow text-success mx-2" role="status"></div>
        <div class="spinner-grow text-warning mx-2" role="status"></div>
      </div>
      <p class="mt-2 text-muted">Procesando transacción...</p>
    `;
    document.getElementById('deposito').appendChild(animacionDiv);
    
    const depositoData = {
      cedulaCliente: cedulaCliente,
      monto: monto,
      numeroCuenta: numeroCuenta || null
    };
    
    try {
      await fetchAPI('/api/transaccion/deposito', {
        method: 'POST',
        body: JSON.stringify(depositoData)
      });
      
      // Cambiar animación a éxito
      animacionDiv.innerHTML = `
        <div class="transaction-success">
          <i class="bi bi-check-circle-fill text-success" style="font-size: 3rem;"></i>
        </div>
        <p class="mt-2 text-success">¡Transacción completada!</p>
      `;
      
      setTimeout(() => {
        if (animacionDiv && animacionDiv.parentNode) {
          animacionDiv.parentNode.removeChild(animacionDiv);
        }
        
        showMessage('Depósito realizado exitosamente', 'success');
        document.getElementById('depositoForm').reset();
        
        // Mostrar un resumen de la operación
        document.getElementById('deposito').innerHTML += `
          <div class="alert alert-success alert-dismissible fade show">
            <div class="d-flex align-items-center">
              <i class="bi bi-check-circle-fill me-2 fs-3"></i>
              <div>
                <h5 class="alert-heading">¡Depósito exitoso!</h5>
                <p class="mb-0">Se depositaron $${monto.toFixed(2)} a la cuenta del cliente con cédula ${cedulaCliente}.</p>
              </div>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
          </div>
        `;
      }, 1500);
    } catch (error) {
      // Cambiar animación a error
      animacionDiv.innerHTML = `
        <div class="transaction-error">
          <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 3rem;"></i>
        </div>
        <p class="mt-2 text-danger">Error en la transacción</p>
      `;
      
      setTimeout(() => {
        if (animacionDiv && animacionDiv.parentNode) {
          animacionDiv.parentNode.removeChild(animacionDiv);
        }
        console.error('Error al realizar depósito:', error);
        showMessage(`Error al realizar depósito: ${error.message}`, 'danger');
      }, 1500);
    } finally {
      setTimeout(() => {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = btnText;
      }, 1000);
    }
  });
  
  // Configurar evento para retiro
  document.getElementById('retiroForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaClienteRetiro').value;
    const monto = parseFloat(document.getElementById('montoRetiro').value);
    const numeroCuenta = document.getElementById('numeroCuentaRetiro').value;
    
    if (!cedulaCliente) {
      showMessage('Ingrese la cédula del cliente', 'warning');
      return;
    }
    
    if (!monto || monto <= 0) {
      showMessage('Ingrese un monto válido mayor a cero', 'warning');
      return;
    }
    
    // Mostrar indicador de carga y deshabilitar botón
    const btnSubmit = e.target.querySelector('button[type="submit"]');
    const btnText = btnSubmit.innerHTML;
    btnSubmit.disabled = true;
    btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...`;
    
    // Crear div de animación
    const animacionDiv = document.createElement('div');
    animacionDiv.className = 'text-center my-4 transaction-animation';
    animacionDiv.innerHTML = `
      <div class="transaction-progress">
        <div class="spinner-grow text-primary mx-2" role="status"></div>
        <div class="spinner-grow text-success mx-2" role="status"></div>
        <div class="spinner-grow text-warning mx-2" role="status"></div>
      </div>
      <p class="mt-2 text-muted">Procesando transacción...</p>
    `;
    document.getElementById('retiro').appendChild(animacionDiv);
    
    const retiroData = {
      cedulaCliente: cedulaCliente,
      monto: monto,
      numeroCuenta: numeroCuenta || null
    };
    
    try {
      await fetchAPI('/api/transaccion/retiro', {
        method: 'POST',
        body: JSON.stringify(retiroData)
      });
      
      // Cambiar animación a éxito
      animacionDiv.innerHTML = `
        <div class="transaction-success">
          <i class="bi bi-check-circle-fill text-success" style="font-size: 3rem;"></i>
        </div>
        <p class="mt-2 text-success">¡Transacción completada!</p>
      `;
      
      setTimeout(() => {
        if (animacionDiv && animacionDiv.parentNode) {
          animacionDiv.parentNode.removeChild(animacionDiv);
        }
        
        showMessage('Retiro realizado exitosamente', 'success');
        document.getElementById('retiroForm').reset();
        
        // Mostrar un resumen de la operación
        document.getElementById('retiro').innerHTML += `
          <div class="alert alert-success alert-dismissible fade show">
            <div class="d-flex align-items-center">
              <i class="bi bi-check-circle-fill me-2 fs-3"></i>
              <div>
                <h5 class="alert-heading">¡Retiro exitoso!</h5>
                <p class="mb-0">Se retiraron $${monto.toFixed(2)} de la cuenta del cliente con cédula ${cedulaCliente}.</p>
              </div>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
          </div>
        `;
      }, 1500);
    } catch (error) {
      // Cambiar animación a error
      animacionDiv.innerHTML = `
        <div class="transaction-error">
          <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 3rem;"></i>
        </div>
        <p class="mt-2 text-danger">Error en la transacción</p>
      `;
      
      setTimeout(() => {
        if (animacionDiv && animacionDiv.parentNode) {
          animacionDiv.parentNode.removeChild(animacionDiv);
        }
        console.error('Error al realizar retiro:', error);
        showMessage(`Error al realizar retiro: ${error.message}`, 'danger');
      }, 1500);
    } finally {
      setTimeout(() => {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = btnText;
      }, 1000);
    }
  });
  
  // Configurar evento para transferencia
  document.getElementById('transferenciaForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaOrigen = document.getElementById('cedulaOrigen').value;
    const cedulaDestino = document.getElementById('cedulaDestino').value;
    const monto = parseFloat(document.getElementById('montoTransferencia').value);
    const descripcion = document.getElementById('descripcionTransferencia').value;
    
    if (!cedulaOrigen) {
      showMessage('Ingrese la cédula del cliente origen', 'warning');
      return;
    }
    
    if (!cedulaDestino) {
      showMessage('Ingrese la cédula del cliente destino', 'warning');
      return;
    }
    
    if (!monto || monto <= 0) {
      showMessage('Ingrese un monto válido mayor a cero', 'warning');
      return;
    }
    
    if (cedulaOrigen === cedulaDestino) {
      showMessage('Las cédulas de origen y destino no pueden ser iguales', 'warning');
      return;
    }
    
    // Verificar que se hayan consultado ambos clientes
    if (document.getElementById('infoClienteOrigen').classList.contains('d-none')) {
      showMessage('Debe verificar el cliente origen primero', 'warning');
      return;
    }
    
    if (document.getElementById('infoClienteDestino').classList.contains('d-none')) {
      showMessage('Debe verificar el cliente destino primero', 'warning');
      return;
    }
    
    // Mostrar indicador de carga y deshabilitar botón
    const btnSubmit = e.target.querySelector('button[type="submit"]');
    const btnText = btnSubmit.innerHTML;
    btnSubmit.disabled = true;
    btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...`;
    
    // Crear div de animación
    const animacionDiv = document.createElement('div');
    animacionDiv.className = 'text-center my-4 transaction-animation';
    animacionDiv.innerHTML = `
      <div class="transaction-progress">
        <div class="spinner-grow text-primary mx-2" role="status"></div>
        <div class="spinner-grow text-success mx-2" role="status"></div>
        <div class="spinner-grow text-warning mx-2" role="status"></div>
      </div>
      <p class="mt-2 text-muted">Procesando transferencia...</p>
    `;
    document.getElementById('transferencia').appendChild(animacionDiv);
    
    const transferenciaData = {
      cedulaClienteOrigen: cedulaOrigen,
      cedulaClienteDestino: cedulaDestino,
      monto: monto,
      descripcion: descripcion || undefined
    };
    
    try {
      const resultado = await fetchAPI('/api/transaccion/transferencia', {
        method: 'POST',
        body: JSON.stringify(transferenciaData)
      });
      
      if (resultado && resultado.exitoso) {
        // Cambiar animación a éxito
        animacionDiv.innerHTML = `
          <div class="transaction-success">
            <i class="bi bi-check-circle-fill text-success" style="font-size: 3rem;"></i>
          </div>
          <p class="mt-2 text-success">¡Transferencia completada!</p>
        `;
        
        setTimeout(() => {
          if (animacionDiv && animacionDiv.parentNode) {
            animacionDiv.parentNode.removeChild(animacionDiv);
          }
          
          showMessage('Transferencia realizada exitosamente', 'success');
          document.getElementById('transferenciaForm').reset();
          document.getElementById('infoClienteOrigen').classList.add('d-none');
          document.getElementById('infoClienteDestino').classList.add('d-none');
          
          // Mostrar un resumen de la operación
          document.getElementById('transferencia').innerHTML += `
            <div class="alert alert-success alert-dismissible fade show">
              <div class="d-flex align-items-center">
                <i class="bi bi-check-circle-fill me-2 fs-3"></i>
                <div>
                  <h5 class="alert-heading">¡Transferencia exitosa!</h5>
                  <p>Se transfirieron $${monto.toFixed(2)} desde la cuenta del cliente ${cedulaOrigen} a la cuenta del cliente ${cedulaDestino}.</p>
                  ${descripcion ? `<p class="mb-0"><strong>Descripción:</strong> ${descripcion}</p>` : ''}
                </div>
              </div>
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
          `;
        }, 1500);
      } else {
        // Cambiar animación a error
        animacionDiv.innerHTML = `
          <div class="transaction-error">
            <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 3rem;"></i>
          </div>
          <p class="mt-2 text-danger">Error en la transferencia</p>
        `;
        
        setTimeout(() => {
          if (animacionDiv && animacionDiv.parentNode) {
            animacionDiv.parentNode.removeChild(animacionDiv);
          }
          showMessage(`No se pudo realizar la transferencia: ${resultado ? resultado.mensaje : 'Error desconocido'}`, 'danger');
        }, 1500);
      }
    } catch (error) {
      // Cambiar animación a error
      animacionDiv.innerHTML = `
        <div class="transaction-error">
          <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 3rem;"></i>
        </div>
        <p class="mt-2 text-danger">Error en la transferencia</p>
      `;
      
      setTimeout(() => {
        if (animacionDiv && animacionDiv.parentNode) {
          animacionDiv.parentNode.removeChild(animacionDiv);
        }
        console.error('Error al realizar transferencia:', error);
        showMessage(`Error al realizar transferencia: ${error.message}`, 'danger');
      }, 1500);
    } finally {
      setTimeout(() => {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = btnText;
      }, 1000);
    }
  });
  
  // Configurar evento para consultar histórico
  document.getElementById('historicoForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaClienteHistorico').value;
    const transaccionesHistorico = document.getElementById('transaccionesHistorico');
    
    if (!cedulaCliente) {
      showMessage('Ingrese la cédula del cliente', 'warning');
      return;
    }
    
    // Mostrar indicador de carga
    transaccionesHistorico.innerHTML = `
      <div class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
        <p class="mt-2">Consultando historial de transacciones...</p>
      </div>
    `;
    
    try {
      // Primero verificamos que el cliente exista
      await fetchAPI(`/api/clientes/document/${cedulaCliente}`).catch(() => {
        throw new Error(`No existe un cliente con cédula ${cedulaCliente}`);
      });
      
      const response = await fetchAPI(`/api/transaccion/${cedulaCliente}`);
      
      // Procesar la respuesta de lista enlazada
      const transacciones = [];
      let nodoActual = response?.cabeza;
      
      // Recorrer la lista enlazada para extraer las transacciones
      while (nodoActual) {
        transacciones.push({
          ...nodoActual.dato,
          tipo: nodoActual.dato.tipoTransaccion, // Adaptación para mantener compatibilidad
          fecha: nodoActual.dato.fecha,
          monto: nodoActual.dato.monto
        });
        nodoActual = nodoActual.siguiente;
      }
      
      if (transacciones && transacciones.length > 0) {
        // Agrupar por fecha (solo fecha, no hora)
        const transaccionesPorFecha = {};
        transacciones.forEach(t => {
          const fecha = new Date(t.fecha).toLocaleDateString();
          if (!transaccionesPorFecha[fecha]) {
            transaccionesPorFecha[fecha] = [];
          }
          transaccionesPorFecha[fecha].push(t);
        });
        
        let html = `
          <div class="card">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">Historial de Transacciones</h5>
              <span class="badge bg-light text-dark">${transacciones.length} transacciones</span>
            </div>
            <div class="card-body">
        `;
        
        // Obtener las fechas y ordenarlas de más reciente a más antigua
        const fechas = Object.keys(transaccionesPorFecha).sort((a, b) => {
          return new Date(b) - new Date(a);
        });
        
        fechas.forEach(fecha => {
          const transaccionesDelDia = transaccionesPorFecha[fecha];
          
          html += `
            <div class="mb-4">
              <h6 class="border-bottom pb-2">${fecha}</h6>
              <div class="table-responsive">
                <table class="table table-striped table-hover">
                  <thead class="table-light">
                    <tr>
                      <th>Hora</th>
                      <th>Tipo</th>
                      <th>Monto</th>
                    </tr>
                  </thead>
                  <tbody>
          `;
          
          transaccionesDelDia.forEach(t => {
            const hora = new Date(t.fecha).toLocaleTimeString();
            let badgeClass = '';
            
            switch(t.tipo) {
              case 'DEPOSITO':
                badgeClass = 'bg-success';
                break;
              case 'RETIRO':
                badgeClass = 'bg-warning text-dark';
                break;
              case 'TRANSFERENCIA_ENVIADA':
              case 'TRANSFERENCIA_SALIENTE':
                badgeClass = 'bg-danger';
                break;
              case 'TRANSFERENCIA_RECIBIDA':
              case 'TRANSFERENCIA_ENTRANTE':
                badgeClass = 'bg-success';
                break;
              default:
                badgeClass = 'bg-secondary';
            }
            
            html += `
              <tr>
                <td>${hora}</td>
                <td><span class="badge ${badgeClass}">${formatearTipoTransaccion(t.tipo)}</span></td>
                <td class="${t.tipo === 'DEPOSITO' || t.tipo === 'TRANSFERENCIA_RECIBIDA' || t.tipo === 'TRANSFERENCIA_ENTRANTE' ? 'text-success' : 'text-danger'}">
                  ${t.tipo === 'DEPOSITO' || t.tipo === 'TRANSFERENCIA_RECIBIDA' || t.tipo === 'TRANSFERENCIA_ENTRANTE' ? '+' : '-'}$${t.monto.toFixed(2)}
                </td>
              </tr>
            `;
          });
          
          html += `</tbody></table></div></div>`;
        });
        
        html += `</div></div>`;
        transaccionesHistorico.innerHTML = html;
      } else {
        transaccionesHistorico.innerHTML = `
          <div class="alert alert-info">
            <div class="d-flex align-items-center">
              <i class="bi bi-info-circle-fill me-2 fs-3"></i>
              <div>
                <h5 class="alert-heading">Sin transacciones</h5>
                <p class="mb-0">No hay transacciones registradas para el cliente con cédula ${cedulaCliente}.</p>
              </div>
            </div>
          </div>
        `;
      }
    } catch (error) {
      console.error('Error al consultar transacciones:', error);
      transaccionesHistorico.innerHTML = `
        <div class="alert alert-danger">
          <div class="d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2 fs-3"></i>
            <div>
              <h5 class="alert-heading">Error</h5>
              <p class="mb-0">${error.message || 'No se pudo consultar el historial de transacciones'}</p>
            </div>
          </div>
        </div>
      `;

    }
  });
  
  // Función auxiliar para formatear el tipo de transacción
  function formatearTipoTransaccion(tipo) {
    switch(tipo) {
      case 'DEPOSITO':
        return 'Depósito';
      case 'RETIRO':
        return 'Retiro';
      case 'TRANSFERENCIA_ENVIADA':
      case 'TRANSFERENCIA_SALIENTE':
        return 'Transferencia enviada';
      case 'TRANSFERENCIA_RECIBIDA':
      case 'TRANSFERENCIA_ENTRANTE':
        return 'Transferencia recibida';
      default:
        return tipo;
    }
  }
}

// ================ MÓDULO PUNTOS ================
function loadPuntos() {
  updateNavbarActiveState('puntos');
  contentElement.innerHTML = `
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">Programa de Puntos</h2>
      <img src="images/loyalty.png" alt="Puntos" class="header-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/2377/2377810.png'; this.onerror='';">
    </div>
    
    <div class="row">
      <div class="col-lg-5">
        <div class="card points-card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Consulta de Puntos</h4>
            <i class="bi bi-star-fill"></i>
          </div>
          <div class="card-body">
            <form id="consultaPuntosForm" class="mb-4">
              <div class="mb-3">
                <label for="cedulaClientePuntos" class="form-label">Cédula del Cliente</label>
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                  <input type="text" class="form-control" id="cedulaClientePuntos" 
                    placeholder="Ingrese la cédula del cliente" required>
                  <button type="submit" class="btn btn-primary">
                    <i class="bi bi-search"></i> Consultar
                  </button>
                </div>
                <small class="text-muted">Consulte los puntos acumulados y beneficios disponibles</small>
              </div>
            </form>
            <div id="puntosDetalles" class="mt-3"></div>
          </div>
        </div>
        
        <div class="card points-card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Categorías de Clientes</h4>
            <i class="bi bi-trophy"></i>
          </div>
          <div class="card-body">
                         <div class="row">
              <div class="col-md-3 text-center mb-3">
                <span class="tier-badge tier-bronze">Bronce</span>
                <p class="mb-0">0-499 pts</p>
              </div>
              <div class="col-md-3 text-center mb-3">
                <span class="tier-badge tier-silver">Plata</span>
                <p class="mb-0">500-999 pts</p>
              </div>
              <div class="col-md-3 text-center mb-3">
                <span class="tier-badge tier-gold">Oro</span>
                <p class="mb-0">1000-4999 pts</p>
              </div>
              <div class="col-md-3 text-center mb-3">
                <span class="tier-badge tier-platinum">Platino</span>
                <p class="mb-0">5000+ pts</p>
              </div>
            </div>
            <div class="card bg-light mt-2">
              <div class="card-body">
                <h6 class="mb-2">¿Cómo acumular puntos?</h6>
                <ul class="mb-0">
                  <li>Depósitos: 1 punto por cada $100</li>
                  <li>Retiros: 2 puntos por cada $100</li>
                  <li>Transferencias: 3 puntos por cada $100</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-7">
        <div class="card points-card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Beneficios Disponibles</h4>
            <i class="bi bi-gift"></i>
          </div>
          <div class="card-body">
            <p class="text-muted mb-4">Los clientes pueden canjear sus puntos por estos beneficios según su categoría</p>
            <div id="beneficiosLista"></div>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Cargar beneficios disponibles
  fetchBeneficios();
  
  // Configurar evento para consultar puntos
  document.getElementById('consultaPuntosForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaClientePuntos').value;
    
    if (!cedulaCliente) {
      showMessage('Ingrese la cédula del cliente', 'warning');
      return;
    }
    
    // Mostrar indicador de carga
    document.getElementById('puntosDetalles').innerHTML = `
      <div class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
        <p class="mt-2 text-muted">Consultando información de puntos...</p>
      </div>
    `;
    
    try {
      // Verificar que el cliente exista
      try {
        await fetchAPI(`/api/clientes/document/${cedulaCliente}`);
      } catch (error) {
        document.getElementById('puntosDetalles').innerHTML = `
          <div class="alert alert-danger">
            <h5><i class="bi bi-exclamation-triangle-fill"></i> Cliente no encontrado</h5>
            <p class="mb-0">No existe un cliente registrado con la cédula ${cedulaCliente}.</p>
          </div>
        `;
        return;
      }
      
      // Obtener información de puntos
      const puntosInfo = await fetchAPI(`/puntos/${cedulaCliente}`);
      
      // Calcular porcentaje para la barra de progreso
      let porcentajeProgreso = 0;
      let siguienteNivel = "";
      
      if (puntosInfo.rango === "BRONCE") {
        porcentajeProgreso = (puntosInfo.puntosAcumulados / 500) * 100;
        siguienteNivel = "Plata";
      } else if (puntosInfo.rango === "PLATA") {
        porcentajeProgreso = ((puntosInfo.puntosAcumulados - 500) / 500) * 100;
        siguienteNivel = "Oro";
      } else if (puntosInfo.rango === "ORO") {
        porcentajeProgreso = ((puntosInfo.puntosAcumulados - 1000) / 4000) * 100;
        siguienteNivel = "Platino";
      } else {
        porcentajeProgreso = 100;
        siguienteNivel = "Máximo nivel alcanzado";
      }
      
      // Limitar porcentaje a 100%
      porcentajeProgreso = Math.min(porcentajeProgreso, 100);
      
      // Determinar clase para el badge de rango
      let tierClass = "";
      switch(puntosInfo.rango) {
        case "BRONCE":
          tierClass = "tier-bronze";
          break;
        case "PLATA":
          tierClass = "tier-silver";
          break;
        case "ORO":
          tierClass = "tier-gold";
          break;
        case "PLATINO":
          tierClass = "tier-platinum";
          break;
        default:
          tierClass = "bg-secondary";
      }
      
      document.getElementById('puntosDetalles').innerHTML = `
        <div class="card shadow-sm">
          <div class="card-body p-4">
            <div class="text-center mb-4">
              <span class="tier-badge ${tierClass}">${puntosInfo.rango}</span>
            </div>
            <div class="points-counter">${puntosInfo.puntosAcumulados}</div>
            <div class="points-label">Puntos acumulados</div>
            
            <div class="mb-3">
              <div class="d-flex justify-content-between mb-1">
                <span>Progreso hacia ${siguienteNivel}</span>
                <span>${Math.round(porcentajeProgreso)}%</span>
              </div>
              <div class="progress progress-points">
                <div class="progress-bar bg-purple" role="progressbar" style="width: ${porcentajeProgreso}%;" 
                  aria-valuenow="${porcentajeProgreso}" aria-valuemin="0" aria-valuemax="100"></div>
              </div>
              <small class="text-muted">Faltan ${puntosInfo.puntosParaSiguienteRango} puntos para el siguiente nivel</small>
            </div>
          </div>
        </div>
      `;
    } catch (error) {
      document.getElementById('puntosDetalles').innerHTML = `
        <div class="alert alert-danger">
          <h5><i class="bi bi-exclamation-triangle-fill"></i> Error</h5>
          <p class="mb-0">${error.message || 'No se pudo consultar la información de puntos'}</p>
        </div>
      `;
      console.error('Error al consultar puntos:', error);
    }
  });
}

async function fetchBeneficios() {
  try {
    const beneficios = await fetchAPI('/puntos/beneficios-disponibles');
    const beneficiosLista = document.getElementById('beneficiosLista');
    
    beneficiosLista.innerHTML = '';
    
    if (beneficios && beneficios.length > 0) {
      // Definir íconos por tipo de beneficio
      const iconosPorTipo = {
        'DESCUENTO': 'bi-tag-fill',
        'CASHBACK': 'bi-cash-coin',
        'REGALO': 'bi-gift-fill',
        'VIAJE': 'bi-airplane-fill',
        'EXPERIENCIA': 'bi-stars',
        'SERVICIO': 'bi-headset',
        'PREMIUM': 'bi-gem',
        'SEGURO': 'bi-shield-check',
        'COMPRAS': 'bi-cart-fill',
        'TECNOLOGIA': 'bi-laptop',
        'ENTRETENIMIENTO': 'bi-film',
        'RESTAURANTE': 'bi-cup-hot-fill'
      };
      
      let html = '';
      
      beneficios.forEach(b => {
        // Obtener ícono según tipo o usar uno predeterminado
        const icono = iconosPorTipo[b.tipo] || 'bi-award';
        

        
        html += `
          <div class="card benefit-card mb-3">
            <div class="card-body">
              <div class="d-flex align-items-center">
                <div class="benefit-icon">
                  <i class="bi ${icono}"></i>
                </div>
                <div class="flex-grow-1">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="mb-0">${b.tipo.replace(/_/g, ' ').charAt(0).toUpperCase() + b.tipo.replace(/_/g, ' ').slice(1).toLowerCase()}</h5>
                    <span class="badge bg-purple" style="font-size: 0.9rem;">
                      <i class="bi bi-star-fill me-1"></i> ${b.puntosRequeridos} pts
                    </span>
                  </div>
                  <p class="mb-0 text-muted">${b.descripcion}</p>
                  <div class="d-flex justify-content-between align-items-center mt-2">
                    <small class="text-muted">
                      <i class="bi bi-info-circle me-1"></i> Requiere ${b.puntosRequeridos} puntos acumulados
                    </small>
                    <button type="button" class="btn btn-sm btn-outline-purple" onclick="canjearBeneficio('${b.tipo}', ${b.puntosRequeridos})">
                      <i class="bi bi-gift me-1"></i> Canjear
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        `;
      });
      
      beneficiosLista.innerHTML = html;
      

    } else {
      beneficiosLista.innerHTML = `
        <div class="text-center py-4">
          <img src="images/no-benefits.png" alt="Sin beneficios" style="max-width: 120px;" 
              onerror="this.src='https://cdn-icons-png.flaticon.com/512/6598/6598519.png'; this.onerror='';">
          <p class="mt-3 text-muted">No hay beneficios disponibles en este momento</p>
        </div>
      `;
    }
  } catch (error) {
    console.error('Error al cargar beneficios:', error);
    beneficiosLista.innerHTML = `
      <div class="alert alert-danger">
        <div class="d-flex align-items-center">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          <div>
            <h6 class="alert-heading mb-1">Error al cargar beneficios</h6>
            <p class="mb-0 small">No se pudieron cargar los beneficios disponibles. Intente nuevamente más tarde.</p>
          </div>
        </div>
      </div>
    `;
  }
}

// ================ MÓDULO TRANSACCIONES PROGRAMADAS ================
function loadTransaccionesProgramadas() {
  updateNavbarActiveState('programadas');
  contentElement.innerHTML = `
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">Transacciones Programadas</h2>
      <img src="images/schedule.png" alt="Programadas" class="header-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/2693/2693507.png'; this.onerror='';">
    </div>
    
    <div class="row">
      <div class="col-lg-5">
        <div class="card transaction-card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center bg-purple text-white">
            <h4 class="mb-0">Programar Transacción</h4>
            <i class="bi bi-calendar-plus"></i>
          </div>
          <div class="card-body">
            <form id="programarTransaccionForm">
              <div class="mb-3">
                <label for="tipoTransaccion" class="form-label">Tipo de Transacción</label>
                <select class="form-select" id="tipoTransaccion" required onchange="cambiarFormularioProgramado()">
                  <option value="DEPOSITO">Depósito</option>
                  <option value="RETIRO">Retiro</option>
                  <option value="TRANSFERENCIA_SALIENTE">Transferencia</option>
                </select>
              </div>
              
              <div class="mb-3" id="campoClienteOrigen">
                <label for="clienteOrigen" class="form-label">Cédula Cliente Origen</label>
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                  <input type="text" class="form-control" id="clienteOrigen" required 
                    placeholder="Ingrese la cédula del cliente">
                  <button type="button" class="btn btn-outline-primary" onclick="buscarClienteProgramado('origen')">
                    <i class="bi bi-search"></i>
                  </button>
                </div>
              </div>
              
              <div class="mb-3 d-none" id="campoClienteDestino">
                <label for="clienteDestino" class="form-label">Cédula Cliente Destino</label>
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                  <input type="text" class="form-control" id="clienteDestino" 
                    placeholder="Ingrese la cédula del destinatario">
                  <button type="button" class="btn btn-outline-success" onclick="buscarClienteProgramado('destino')">
                    <i class="bi bi-search"></i>
                  </button>
                </div>
              </div>
              
              <div class="mb-3">
                <label for="monto" class="form-label">Monto</label>
                <div class="input-group">
                  <span class="input-group-text">$</span>
                  <input type="number" class="form-control" id="monto" required min="1">
                </div>
              </div>
              
              <div class="mb-3">
                <label for="fechaEjecucion" class="form-label">Fecha de Ejecución</label>
                <input type="datetime-local" class="form-control" id="fechaEjecucion" required>
              </div>
              
              <div class="mb-3">
                <label for="periodicidad" class="form-label">Periodicidad</label>
                <select class="form-select" id="periodicidad" required>
                  <option value="UNICA">Única vez</option>
                  <option value="DIARIA">Diaria</option>
                  <option value="SEMANAL">Semanal</option>
                  <option value="MENSUAL">Mensual</option>
                </select>
              </div>
              
              <div class="text-end">
                <button type="submit" class="btn btn-purple">
                  <i class="bi bi-calendar-check"></i> Programar Transacción
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      
      <div class="col-lg-7">
        <div class="card transaction-card">
          <div class="card-header d-flex justify-content-between align-items-center bg-purple text-white">
            <h4 class="mb-0">Transacciones Programadas</h4>
            <i class="bi bi-calendar-event"></i>
          </div>
          <div class="card-body">
            <form id="consultaProgramadasForm" class="mb-4">
              <label for="numeroCuentaConsulta" class="form-label">Consultar transacciones programadas</label>
              <div class="input-group">
                <span class="input-group-text"><i class="bi bi-person-vcard"></i></span>
                <input type="text" class="form-control" id="cedulaClienteConsulta" 
                  placeholder="Ingrese la cédula del cliente" required>
                <button type="submit" class="btn btn-purple">
                  <i class="bi bi-search"></i> Consultar
                </button>
              </div>
            </form>
            <div id="transaccionesProgramadasLista"></div>
            <div class="card bg-light mt-3 w-100">
              <div class="card-body">
                <h6 class="mb-2">Información:</h6>
                <ul class="mb-0">
                  <li>Las transacciones programadas se ejecutarán según la periodicidad establecida</li>
                  <li>Puede cancelar una transacción programada en cualquier momento</li>
                  <li>Asegúrese de tener fondos suficientes para las transacciones recurrentes</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Configurar evento para programar transacción
  document.getElementById('programarTransaccionForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const tipoTransaccion = document.getElementById('tipoTransaccion').value;
    const monto = parseFloat(document.getElementById('monto').value);
    const fechaEjecucion = document.getElementById('fechaEjecucion').value;
    const periodicidad = document.getElementById('periodicidad').value;
    const clienteOrigen = document.getElementById('clienteOrigen').value;
    
    if (!clienteOrigen) {
      showMessage('Ingrese la cédula del cliente origen', 'warning');
      return;
    }
    
    if (!monto || monto <= 0) {
      showMessage('Ingrese un monto válido mayor a cero', 'warning');
      return;
    }
    
    if (!fechaEjecucion) {
      showMessage('Seleccione una fecha de ejecución', 'warning');
      return;
    }
    
    // Objeto base para todos los tipos de transacciones
    const programadaData = {
      clienteOrigen: clienteOrigen,
      monto: monto,
      tipoTransaccion: tipoTransaccion,
      fechaEjecucion: fechaEjecucion,
      periodicidad: periodicidad
    };
    
    // Si es transferencia, agregar el cliente destino
    if (tipoTransaccion === 'TRANSFERENCIA_SALIENTE') {
      const clienteDestino = document.getElementById('clienteDestino').value;
      
      if (!clienteDestino) {
        showMessage('Ingrese la cédula del cliente destino', 'warning');
        return;
      }
      
      programadaData.clienteDestino = clienteDestino;
    }
    
    // Mostrar indicador de carga y deshabilitar botón
    const btnSubmit = e.target.querySelector('button[type="submit"]');
    const btnText = btnSubmit.innerHTML;
    btnSubmit.disabled = true;
    btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...`;
    
    try {
      const response = await fetchAPI('/api/transacciones-programadas', {
        method: 'POST',
        body: JSON.stringify(programadaData)
      });
      
      // Extract ID from response
      const id = response?.id || response;
      
      showMessage(`Transacción programada exitosamente. ID: ${id}`, 'success');
      
      // Reset form and fields
      resetProgramarTransaccionForm();
      
      // Actualizar lista si hay una consulta activa
      const cedulaClienteConsulta = document.getElementById('cedulaClienteConsulta');
      if (cedulaClienteConsulta && cedulaClienteConsulta.value) {
        document.getElementById('consultaProgramadasForm').dispatchEvent(new Event('submit'));
      }
    } catch (error) {
      console.error('Error al programar transacción:', error);
      showMessage(`Error al programar transacción: ${error.message}`, 'danger');
    } finally {
      btnSubmit.disabled = false;
      btnSubmit.innerHTML = btnText;
    }
  });
  
  // Configurar evento para consultar transacciones programadas
  document.getElementById('consultaProgramadasForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const cedulaCliente = document.getElementById('cedulaClienteConsulta').value;
    
    if (!cedulaCliente) {
      showMessage('Ingrese la cédula del cliente', 'warning');
      return;
    }
    
    const transaccionesProgramadasLista = document.getElementById('transaccionesProgramadasLista');
    
    // Mostrar indicador de carga
    transaccionesProgramadasLista.innerHTML = `
      <div class="text-center py-4">
        <div class="spinner-border text-purple" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
        <p class="mt-2">Consultando transacciones programadas...</p>
      </div>
    `;
    
    try {
      const transaccionesProgramadas = await fetchAPI(`/api/transacciones-programadas/cliente/${cedulaCliente}`);
      
      transaccionesProgramadasLista.innerHTML = '';
      
      if (transaccionesProgramadas && transaccionesProgramadas.length > 0) {
        // Agrupar por tipo de transacción
        const tiposTransaccion = {
          DEPOSITO: [],
          RETIRO: [],
          TRANSFERENCIA_SALIENTE: []
        };
        
        transaccionesProgramadas.forEach(tp => {
          if (tiposTransaccion.hasOwnProperty(tp.tipoTransaccion)) {
            tiposTransaccion[tp.tipoTransaccion].push(tp);
          } else {
            tiposTransaccion.DEPOSITO.push(tp);
          }
        });
        
        let html = '';
        
        // Función para formatear la periodicidad
        const formatearPeriodicidad = (periodicidad) => {
          switch(periodicidad) {
            case 'UNICA': return 'Única vez';
            case 'DIARIA': return 'Diaria';
            case 'SEMANAL': return 'Semanal';
            case 'MENSUAL': return 'Mensual';
            default: return periodicidad;
          }
        };
        
        // Función para obtener el color según el tipo
        const colorTipoTransaccion = (tipo) => {
          switch(tipo) {
            case 'DEPOSITO': return 'success';
            case 'RETIRO': return 'warning';
            case 'TRANSFERENCIA_SALIENTE': return 'danger';
            default: return 'secondary';
          }
        };
        
        // Función para formatear el tipo de transacción
        const formatearTipoTransaccion = (tipo) => {
          switch(tipo) {
            case 'DEPOSITO': return 'Depósito';
            case 'RETIRO': return 'Retiro';
            case 'TRANSFERENCIA_SALIENTE': return 'Transferencia';
            default: return tipo;
          }
        };
        
        // Mostrar las transacciones programadas por tipo
        Object.keys(tiposTransaccion).forEach(tipo => {
          const transacciones = tiposTransaccion[tipo];
          if (transacciones.length === 0) return;
          
          const color = colorTipoTransaccion(tipo);
          
          html += `
            <div class="mb-4">
              <h5 class="border-bottom pb-2">
                <span class="badge bg-${color} me-2">${formatearTipoTransaccion(tipo)}</span>
                <small class="text-muted">${transacciones.length} transacción(es)</small>
              </h5>
          `;
          
          transacciones.forEach(tp => {
            html += `
              <div class="card scheduled-transaction mb-3">
                <div class="card-body">
                  <span class="badge bg-${color} badge-schedule">${formatearPeriodicidad(tp.periodicidad)}</span>
                  <div class="row">
                    <div class="col-md-8">
                      <h5 class="card-title">
                        <i class="bi bi-${tipo === 'DEPOSITO' ? 'arrow-down-circle' : tipo === 'RETIRO' ? 'arrow-up-circle' : 'arrow-left-right'}"></i>
                        ${formatearTipoTransaccion(tp.tipoTransaccion)}
                      </h5>
                      <p class="card-text mb-1">
                        <strong>Monto:</strong> $${tp.monto.toFixed(2)}
                      </p>
                      <p class="card-text mb-1">
                        <strong>Cliente Origen:</strong> ${tp.clienteOrigen}
                      </p>
                      ${tp.clienteDestino ? `<p class="card-text mb-1"><strong>Cliente Destino:</strong> ${tp.clienteDestino}</p>` : ''}
                      <p class="card-text">
                        <small class="text-muted">
                          <i class="bi bi-calendar-event"></i> 
                          Próxima ejecución: ${new Date(tp.fechaEjecucion).toLocaleString()}
                        </small>
                      </p>
                    </div>
                    <div class="col-md-4 d-flex align-items-center justify-content-end">
                      <button class="btn btn-sm btn-outline-danger" onclick="cancelarTransaccionProgramada('${tp.id}')">
                        <i class="bi bi-x-circle"></i> Cancelar
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            `;
          });
          
          html += `</div>`;
        });
        
        transaccionesProgramadasLista.innerHTML = html;
      } else {
        transaccionesProgramadasLista.innerHTML = `
          <div class="alert alert-info">
            <div class="d-flex align-items-center">
              <i class="bi bi-info-circle-fill me-2 fs-3"></i>
              <div>
                <h5 class="alert-heading">Sin transacciones programadas</h5>
                <p class="mb-0">No hay transacciones programadas para el cliente con cédula ${cedulaCliente}.</p>
              </div>
            </div>
          </div>
        `;
      }
    } catch (error) {
      console.error('Error al consultar transacciones programadas:', error);
      transaccionesProgramadasLista.innerHTML = `
        <div class="alert alert-danger">
          <div class="d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2 fs-3"></i>
            <div>
              <h5 class="alert-heading">Error</h5>
              <p class="mb-0">${error.message || 'No se pudieron consultar las transacciones programadas'}</p>
            </div>
          </div>
        </div>
      `;
    }
  });
}

async function cancelarTransaccionProgramada(id) {
  if (!confirm('¿Está seguro que desea cancelar esta transacción programada?')) {
    return;
  }
  
  try {
    // Mostrar indicador de carga
    const btnCancelar = event.target.closest('button');
    const btnText = btnCancelar.innerHTML;
    btnCancelar.disabled = true;
    btnCancelar.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Cancelando...`;
    
    const response = await fetchAPI(`/api/transacciones-programadas/${id}`, {
      method: 'DELETE'
    });
    
    // Process success response
    if (response && response.success) {
      showMessage('Transacción programada cancelada exitosamente', 'success');
      
      // Refrescar lista
      document.getElementById('consultaProgramadasForm').dispatchEvent(new Event('submit'));
    } else {
      throw new Error('No se pudo cancelar la transacción');
    }
  } catch (error) {
    console.error('Error al cancelar transacción programada:', error);
    showMessage(`Error al cancelar la transacción programada: ${error.message}`, 'danger');
    
    // Restaurar botón
    if (btnCancelar) {
      btnCancelar.disabled = false;
      btnCancelar.innerHTML = btnText;
    }
  }
}

// Función para buscar cliente para transacción (depósito/retiro)
function buscarClienteParaTransaccion(idCampo) {
  const cedula = document.getElementById(idCampo).value;
  
  if (!cedula) {
    showMessage('Ingrese una cédula para buscar el cliente', 'warning');
    return;
  }
  
  showMessage('Buscando cliente...', 'info');
  
  fetchAPI(`/api/clientes/document/${cedula}`)
    .then(cliente => {
      showMessage(`Cliente encontrado: ${cliente.nombre} ${cliente.apellido}`, 'success');
      
      // Buscar cuenta del cliente
      return fetchAPI(`/api/cuenta/buscar/${cedula}`);
    })
    .then(cuenta => {
      if (idCampo === 'cedulaClienteDeposito' && document.getElementById('numeroCuentaDeposito')) {
        document.getElementById('numeroCuentaDeposito').value = cuenta.numeroCuenta;
      } else if (idCampo === 'cedulaClienteRetiro' && document.getElementById('numeroCuentaRetiro')) {
        document.getElementById('numeroCuentaRetiro').value = cuenta.numeroCuenta;
      }
    })
    .catch(error => {
      console.error('Error al buscar cliente/cuenta:', error);
    });
}

// Función para buscar cliente para transferencia
function buscarClienteParaTransferencia(tipo) {
  const idCampo = tipo === 'origen' ? 'cedulaOrigen' : 'cedulaDestino';
  const infoCliente = document.getElementById(`infoCliente${tipo.charAt(0).toUpperCase() + tipo.slice(1)}`);
  const detalleCliente = document.getElementById(`detalleCliente${tipo.charAt(0).toUpperCase() + tipo.slice(1)}`);
  const cedula = document.getElementById(idCampo).value;
  
  if (!cedula) {
    showMessage(`Ingrese la cédula del cliente ${tipo}`, 'warning');
    return;
  }
  
  infoCliente.classList.add('d-none');
  showMessage(`Buscando cliente ${tipo}...`, 'info');
  
  fetchAPI(`/api/clientes/document/${cedula}`)
    .then(cliente => {
      // Buscar cuenta del cliente
      return fetchAPI(`/api/cuenta/buscar/${cedula}`)
        .then(cuenta => {
          detalleCliente.innerHTML = `
            <div class="d-flex align-items-center">
              <div>
                <p class="mb-1"><strong>${cliente.nombre} ${cliente.apellido}</strong></p>
                <p class="mb-1"><small>Cuenta: <span class="badge bg-secondary">${cuenta.numeroCuenta}</span></small></p>
                <p class="mb-0"><small>Saldo: $${cuenta.saldoCuenta.toFixed(2)}</small></p>
              </div>
            </div>
          `;
          infoCliente.classList.remove('d-none');
        });
    })
    .catch(error => {
      showMessage(`Error al buscar cliente ${tipo}: ${error.message}`, 'danger');
      console.error(`Error al buscar cliente ${tipo}:`, error);
    });
}

// Función para limpiar formulario de transferencia
function limpiarFormularioTransferencia() {
  document.getElementById('transferenciaForm').reset();
  document.getElementById('infoClienteOrigen').classList.add('d-none');
  document.getElementById('infoClienteDestino').classList.add('d-none');
}



// Cargar el módulo de clientes por defecto al iniciar
document.addEventListener('DOMContentLoaded', () => {
  loadClientes();
});

// Función para canjear un beneficio específico
function canjearBeneficio(tipoBeneficio, puntosRequeridos) {
  // Verificar si hay un cliente seleccionado con puntos
  const formulario = document.getElementById('consultaPuntosForm');
  const cedulaInput = document.getElementById('cedulaClientePuntos');
  
  if (!cedulaInput.value) {
    showMessage('Primero debe consultar un cliente para canjear beneficios', 'warning');
    return;
  }
  
  // Obtener los puntos disponibles del cliente
  const puntosDetalles = document.getElementById('puntosDetalles');
  if (!puntosDetalles.innerHTML || puntosDetalles.querySelector('.alert-danger')) {
    showMessage('Primero debe consultar los puntos del cliente', 'warning');
    return;
  }
  
  // Buscar el contador de puntos en el DOM
  const puntosCounter = puntosDetalles.querySelector('.points-counter');
  if (!puntosCounter) {
    showMessage('No se pudo determinar los puntos disponibles del cliente', 'warning');
    return;
  }
  
  const puntosDisponibles = parseInt(puntosCounter.textContent);
  
  // Verificar si tiene suficientes puntos
  if (puntosDisponibles < puntosRequeridos) {
    showMessage(`No tiene suficientes puntos para este beneficio. Necesita ${puntosRequeridos} puntos.`, 'warning');
    return;
  }
  
  // Mostrar indicador de carga
  const beneficiosLista = document.getElementById('beneficiosLista');
  const originalContent = beneficiosLista.innerHTML;
  const loadingIndicator = document.createElement('div');
  loadingIndicator.className = "text-center py-3";
  loadingIndicator.innerHTML = `
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">Cargando...</span>
    </div>
    <p class="mt-2 mb-0">Procesando canje de puntos...</p>
  `;
  
  beneficiosLista.innerHTML = '';
  beneficiosLista.appendChild(loadingIndicator);
  
  // Realizar el canje directamente
  fetchAPI(`/puntos/canjear/${cedulaInput.value}?puntos=${puntosRequeridos}`, {
    method: 'POST'
  })
    .then(response => {
      showMessage(`¡Canje exitoso! Has canjeado ${puntosRequeridos} puntos por el beneficio ${tipoBeneficio.replace(/_/g, ' ').toLowerCase()}.`, 'success');
      
      // Recargar información de puntos
      document.getElementById('consultaPuntosForm').dispatchEvent(new Event('submit'));
      
      // Restaurar la lista de beneficios
      setTimeout(() => {
        // Recargar la lista de beneficios
        fetchBeneficios();
      }, 1000);
    })
    .catch(error => {
      showMessage(`Error al canjear puntos: ${error.message || 'No se pudieron canjear los puntos. Intente nuevamente.'}`, 'danger');
      
      // Restaurar la lista de beneficios
      beneficiosLista.innerHTML = originalContent;
    });
}

// Función para actualizar el estado activo de la barra de navegación
function updateNavbarActiveState(module) {
  // Remover clase active de todos los enlaces
  document.querySelectorAll('.navbar-nav .nav-link').forEach(link => {
    link.classList.remove('active');
  });
  
  // Agregar clase active al enlace seleccionado
  const moduleMap = {
    'clientes': 0,
    'cuentas': 1,
    'monederos': 2,
    'transacciones': 3,
    'puntos': 4,
    'programadas': 5
  };
  
  const index = moduleMap[module];
  if (index !== undefined) {
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    if (navLinks[index]) {
      navLinks[index].classList.add('active');
    }
  }
}

// Cargar el módulo de clientes por defecto al iniciar
document.addEventListener('DOMContentLoaded', () => {
  loadClientes();
});

// ================ MÓDULO MONEDEROS ================
function loadMonederos() {
  updateNavbarActiveState('monederos');
  contentElement.innerHTML = `
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">Gestión de Monederos</h2>
      <img src="images/wallet.png" alt="Monederos" class="header-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/3135/3135715.png'; this.onerror='';">
    </div>
    
    <div class="row">
      <div class="col-lg-6">
        <div class="card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Gestión de Monederos</h4>
            <i class="bi bi-wallet2"></i>
          </div>
          <div class="card-body">
            <ul class="nav nav-tabs mb-4" id="monederosTab" role="tablist">
              <li class="nav-item" role="presentation">
                <button class="nav-link active" id="lista-monederos-tab" data-bs-toggle="tab" data-bs-target="#lista-monederos" type="button" role="tab">
                  <i class="bi bi-list-ul"></i> Mis Monederos
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="crear-monedero-tab" data-bs-toggle="tab" data-bs-target="#crear-monedero" type="button" role="tab">
                  <i class="bi bi-plus-circle"></i> Crear Monedero
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button class="nav-link" id="transferir-tab" data-bs-toggle="tab" data-bs-target="#transferir" type="button" role="tab">
                  <i class="bi bi-arrow-left-right"></i> Transferir
                </button>
              </li>
            </ul>
            
            <div class="tab-content" id="monederosTabContent">
              <!-- Tab de Lista de Monederos -->
              <div class="tab-pane fade show active" id="lista-monederos" role="tabpanel">
                <div class="card">
                  <div class="card-header bg-primary text-white">
                    <h5 class="mb-0">Buscar Monederos por Cliente</h5>
                  </div>
                  <div class="card-body">
                    <form id="buscarMonederosForm" class="mb-3">
                      <div class="input-group">
                        <input type="text" class="form-control" id="cedulaClienteMonederos" placeholder="Cédula del cliente" required>
                        <button class="btn btn-primary" type="submit">
                          <i class="bi bi-search"></i> Buscar
                        </button>
                      </div>
                    </form>
                    <div id="monederosContainer"></div>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Crear Monedero -->
              <div class="tab-pane fade" id="crear-monedero" role="tabpanel">
                <div class="card">
                  <div class="card-header bg-success text-white">
                    <h5 class="mb-0">Crear Nuevo Monedero</h5>
                  </div>
                  <div class="card-body">
                    <form id="crearMonederoForm">
                      <div class="mb-3">
                        <label for="cedulaClienteCrear" class="form-label">Cédula del Cliente</label>
                        <input type="text" class="form-control" id="cedulaClienteCrear" required>
                      </div>
                      <div class="mb-3">
                        <label for="nombreMonedero" class="form-label">Nombre del Monedero</label>
                        <input type="text" class="form-control" id="nombreMonedero" placeholder="Ej: Ahorros para vacaciones" required>
                      </div>
                      <div class="mb-3">
                        <label for="tipoMonedero" class="form-label">Tipo de Monedero</label>
                        <select class="form-select" id="tipoMonedero" required>
                          <option value="">Seleccione un tipo</option>
                          <option value="AHORROS">Ahorros</option>
                          <option value="GASTOS_DIARIOS">Gastos Diarios</option>
                          <option value="EMERGENCIAS">Emergencias</option>
                          <option value="VACACIONES">Vacaciones</option>
                          <option value="INVERSIONES">Inversiones</option>
                        </select>
                      </div>
                      <button type="submit" class="btn btn-success">
                        <i class="bi bi-plus-circle"></i> Crear Monedero
                      </button>
                    </form>
                  </div>
                </div>
              </div>
              
              <!-- Tab de Transferir -->
              <div class="tab-pane fade" id="transferir" role="tabpanel">
                <div class="card">
                  <div class="card-header bg-warning text-dark">
                    <h5 class="mb-0">Transferir Entre Monederos</h5>
                  </div>
                  <div class="card-body">
                    <form id="transferirMonederosForm">
                      <div class="mb-3">
                        <label for="cedulaClienteTransferir" class="form-label">Cédula del Cliente</label>
                        <input type="text" class="form-control" id="cedulaClienteTransferir" required>
                      </div>
                      <div class="row">
                        <div class="col-md-6">
                          <div class="mb-3">
                            <label for="monederoOrigen" class="form-label">Monedero Origen</label>
                            <select class="form-select" id="monederoOrigen" required>
                              <option value="">Seleccione monedero origen</option>
                            </select>
                          </div>
                        </div>
                        <div class="col-md-6">
                          <div class="mb-3">
                            <label for="monederoDestino" class="form-label">Monedero Destino</label>
                            <select class="form-select" id="monederoDestino" required>
                              <option value="">Seleccione monedero destino</option>
                            </select>
                          </div>
                        </div>
                      </div>
                      <div class="mb-3">
                        <label for="montoTransferir" class="form-label">Monto a Transferir</label>
                        <input type="number" class="form-control" id="montoTransferir" step="0.01" min="0.01" required>
                      </div>
                      <div class="mb-3">
                        <label for="conceptoTransferencia" class="form-label">Concepto</label>
                        <input type="text" class="form-control" id="conceptoTransferencia" placeholder="Descripción de la transferencia" required>
                      </div>
                      <button type="button" class="btn btn-info me-2" onclick="cargarMonederosParaTransferencia()">
                        <i class="bi bi-arrow-clockwise"></i> Cargar Monederos
                      </button>
                      <button type="submit" class="btn btn-warning">
                        <i class="bi bi-arrow-left-right"></i> Transferir
                      </button>
                    </form>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-6">
        <div class="card">
          <div class="card-header bg-info text-white">
            <h4 class="mb-0">Visualización de Relaciones</h4>
          </div>
          <div class="card-body">
            <div id="grafoMonederos" class="text-center">
              <p class="text-muted">Seleccione un cliente para ver las relaciones entre monederos</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Configurar eventos
  setupMonederosEvents();
}

function setupMonederosEvents() {
  // Evento para buscar monederos
  document.getElementById('buscarMonederosForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const cedulaCliente = document.getElementById('cedulaClienteMonederos').value;
    await cargarMonederosPorCliente(cedulaCliente);
  });
  
  // Evento para crear monedero
  document.getElementById('crearMonederoForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    await crearNuevoMonedero();
  });
  
  // Evento para transferir entre monederos
  document.getElementById('transferirMonederosForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    await realizarTransferenciaEntreMonederos();
  });
}

async function cargarMonederosPorCuenta(numeroCuenta) {
  try {
    const monederos = await fetchAPI(`/api/monederos/cuenta/${numeroCuenta}`);
    const saldoTotal = await fetchAPI(`/api/monederos/cuenta/${numeroCuenta}/saldo-total`);
    
    mostrarMonederos(monederos, saldoTotal.saldoTotal);
    mostrarGrafoRelaciones(numeroCuenta, monederos);
  } catch (error) {
    showMessage(`Error al cargar monederos: ${error.message}`, 'danger');
  }
}

function mostrarMonederos(monederos, saldoTotal, cedulaCliente) {
  const container = document.getElementById('monederosContainer');
  
  if (!monederos || monederos.length === 0) {
    container.innerHTML = `
      <div class="alert alert-info">
        <i class="bi bi-info-circle"></i> No se encontraron monederos para este cliente.
      </div>
    `;
    return;
  }
  
  container.innerHTML = `
    <div class="row mb-3">
      <div class="col-12">
        <div class="alert alert-success">
          <h5 class="mb-0">
            <i class="bi bi-wallet2"></i> Saldo Total: $${saldoTotal.toLocaleString('es-CO', {minimumFractionDigits: 2})}
          </h5>
        </div>
      </div>
    </div>
    <div class="row">
      ${monederos.map(monedero => `
        <div class="col-md-6 mb-3">
          <div class="card ${monedero.tipo === 'PRINCIPAL' ? 'border-primary' : 'border-secondary'}">
            <div class="card-header ${monedero.tipo === 'PRINCIPAL' ? 'bg-primary text-white' : 'bg-light'}">
              <h6 class="mb-0">
                <i class="bi bi-wallet"></i> ${monedero.nombre}
                ${monedero.tipo === 'PRINCIPAL' ? '<span class="badge bg-warning ms-2">Principal</span>' : ''}
              </h6>
            </div>
            <div class="card-body">
              <p class="card-text">
                <strong>Tipo:</strong> ${formatearTipoMonedero(monedero.tipo)}<br>
                <strong>Saldo:</strong> $${monedero.saldo.toLocaleString('es-CO', {minimumFractionDigits: 2})}<br>
                <strong>ID:</strong> <small class="text-muted">${monedero.id}</small>
              </p>
              <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-outline-info" onclick="verRelacionesMonedero('${monedero.cuentaPropietaria}', '${monedero.id}')">
                  <i class="bi bi-diagram-3"></i> Relaciones
                </button>
                ${monedero.tipo !== 'PRINCIPAL' ? `
                  <button class="btn btn-outline-danger" onclick="eliminarMonedero('${monedero.cuentaPropietaria}', '${monedero.id}', '${monedero.nombre}')">
                    <i class="bi bi-trash"></i> Eliminar
                  </button>
                ` : ''}
              </div>
            </div>
          </div>
        </div>
      `).join('')}
    </div>
  `;
}

function formatearTipoMonedero(tipo) {
  const tipos = {
    'PRINCIPAL': 'Principal',
    'AHORROS': 'Ahorros',
    'GASTOS_DIARIOS': 'Gastos Diarios',
    'EMERGENCIAS': 'Emergencias',
    'VACACIONES': 'Vacaciones',
    'INVERSIONES': 'Inversiones'
  };
  return tipos[tipo] || tipo;
}

async function crearNuevoMonedero() {
  const cedulaCliente = document.getElementById('cedulaClienteCrear').value;
  const nombre = document.getElementById('nombreMonedero').value;
  const tipo = document.getElementById('tipoMonedero').value;
  
  try {
    const monedero = await fetchAPI('/api/monederos/crear', {
      method: 'POST',
      body: JSON.stringify({
        cedulaCliente,
        nombre,
        tipo
      })
    });
    
    showMessage(`Monedero "${nombre}" creado exitosamente`, 'success');
    document.getElementById('crearMonederoForm').reset();
    
    // Si hay monederos cargados, actualizar la lista
    const cedulaClienteActual = document.getElementById('cedulaClienteMonederos').value;
    if (cedulaClienteActual === cedulaCliente) {
      await cargarMonederosPorCliente(cedulaCliente);
    }
  } catch (error) {
    showMessage(`Error al crear monedero: ${error.message}`, 'danger');
  }
}

async function cargarMonederosParaTransferencia() {
  const cedulaCliente = document.getElementById('cedulaClienteTransferir').value;
  
  if (!cedulaCliente) {
    showMessage('Ingrese la cédula del cliente primero', 'warning');
    return;
  }
  
  try {
    const monederos = await fetchAPI(`/api/monederos/cliente/${cedulaCliente}`);
    
    const selectOrigen = document.getElementById('monederoOrigen');
    const selectDestino = document.getElementById('monederoDestino');
    
    // Limpiar opciones anteriores
    selectOrigen.innerHTML = '<option value="">Seleccione monedero origen</option>';
    selectDestino.innerHTML = '<option value="">Seleccione monedero destino</option>';
    
    // Agregar opciones
    monederos.forEach(monedero => {
      const option = `<option value="${monedero.id}">${monedero.nombre} - $${monedero.saldo.toLocaleString('es-CO', {minimumFractionDigits: 2})}</option>`;
      selectOrigen.innerHTML += option;
      selectDestino.innerHTML += option;
    });
    
    showMessage('Monederos cargados correctamente', 'success');
  } catch (error) {
    showMessage(`Error al cargar monederos: ${error.message}`, 'danger');
  }
}

async function realizarTransferenciaEntreMonederos() {
  const cedulaCliente = document.getElementById('cedulaClienteTransferir').value;
  const idMonederoOrigen = document.getElementById('monederoOrigen').value;
  const idMonederoDestino = document.getElementById('monederoDestino').value;
  const monto = parseFloat(document.getElementById('montoTransferir').value);
  const concepto = document.getElementById('conceptoTransferencia').value;
  
  if (idMonederoOrigen === idMonederoDestino) {
    showMessage('El monedero origen y destino no pueden ser el mismo', 'warning');
    return;
  }
  
  try {
    const transaccion = await fetchAPI('/api/monederos/transferir', {
      method: 'POST',
      body: JSON.stringify({
        cedulaCliente,
        idMonederoOrigen,
        idMonederoDestino,
        monto,
        concepto
      })
    });
    
    showMessage(`Transferencia realizada exitosamente. ID: ${transaccion.codigo}`, 'success');
    document.getElementById('transferirMonederosForm').reset();
    
    // Recargar monederos para transferencia
    await cargarMonederosParaTransferencia();
    
    // Si hay monederos cargados en la vista principal, actualizarlos
    const cedulaClienteActual = document.getElementById('cedulaClienteMonederos').value;
    if (cedulaClienteActual === cedulaCliente) {
      await cargarMonederosPorCliente(cedulaCliente);
    }
  } catch (error) {
    showMessage(`Error al realizar transferencia: ${error.message}`, 'danger');
  }
}

async function verRelacionesMonedero(cedulaCliente, idMonedero) {
  try {
    const relaciones = await fetchAPI(`/api/monederos/cliente/${cedulaCliente}/monedero/${idMonedero}/relaciones`);
    
    let relacionesHtml = '<h6>Relaciones del Monedero:</h6>';
    if (relaciones.length === 0) {
      relacionesHtml += '<p class="text-muted">No hay relaciones directas configuradas.</p>';
    } else {
      relacionesHtml += '<ul class="list-group">';
      relaciones.forEach(relacion => {
        relacionesHtml += `
          <li class="list-group-item d-flex justify-content-between align-items-center">
            Destino: ${relacion.destino}
            <span class="badge bg-primary rounded-pill">Comisión: ${relacion.peso}%</span>
          </li>
        `;
      });
      relacionesHtml += '</ul>';
    }
    
    // Mostrar en modal o alert
    const modalHtml = `
      <div class="modal fade" id="relacionesModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Relaciones del Monedero</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              ${relacionesHtml}
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
            </div>
          </div>
        </div>
      </div>
    `;
    
    // Agregar modal al DOM y mostrarlo
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    const modal = new bootstrap.Modal(document.getElementById('relacionesModal'));
    modal.show();
    
    // Limpiar modal cuando se cierre
    document.getElementById('relacionesModal').addEventListener('hidden.bs.modal', function () {
      this.remove();
    });
  } catch (error) {
    showMessage(`Error al cargar relaciones: ${error.message}`, 'danger');
  }
}

async function eliminarMonedero(cedulaCliente, idMonedero, nombreMonedero) {
  if (!confirm(`¿Está seguro de eliminar el monedero "${nombreMonedero}"? Esta acción no se puede deshacer.`)) {
    return;
  }
  
  try {
    await fetchAPI(`/api/monederos/cliente/${cedulaCliente}/monedero/${idMonedero}`, {
      method: 'DELETE'
    });
    
    showMessage(`Monedero "${nombreMonedero}" eliminado exitosamente`, 'success');
    await cargarMonederosPorCliente(cedulaCliente);
  } catch (error) {
    showMessage(`Error al eliminar monedero: ${error.message}`, 'danger');
  }
}

function mostrarGrafoRelaciones(cedulaCliente, monederos) {
  const grafoContainer = document.getElementById('grafoMonederos');
  
  if (!monederos || monederos.length === 0) {
    grafoContainer.innerHTML = '<p class="text-muted">No hay monederos para mostrar relaciones</p>';
    return;
  }
  
  // Crear una representación visual simple del grafo
  let grafoHtml = `
    <h6>Estructura de Monederos</h6>
    <div class="d-flex flex-wrap justify-content-center gap-2">
  `;
  
  monederos.forEach(monedero => {
    const colorClass = monedero.tipo === 'PRINCIPAL' ? 'bg-primary' : 'bg-secondary';
    grafoHtml += `
      <div class="card text-center" style="width: 120px;">
        <div class="card-body p-2 ${colorClass} text-white">
          <small class="card-title">${monedero.nombre}</small>
          <div class="small">$${monedero.saldo.toLocaleString('es-CO', {minimumFractionDigits: 0})}</div>
        </div>
      </div>
    `;
  });
  
  grafoHtml += `
    </div>
    <div class="mt-3">
      <small class="text-muted">
        <i class="bi bi-info-circle"></i> 
        Los monederos están conectados a través del grafo dirigido. 
        El monedero principal (azul) actúa como hub central.
      </small>
    </div>
  `;
  
  grafoContainer.innerHTML = grafoHtml;
}

// Función para cambiar el formulario de transacción programada según el tipo seleccionado
function cambiarFormularioProgramado() {
  const tipoTransaccion = document.getElementById('tipoTransaccion').value;
  const campoClienteDestino = document.getElementById('campoClienteDestino');
  
  if (tipoTransaccion === 'TRANSFERENCIA_SALIENTE') {
    campoClienteDestino.classList.remove('d-none');
    document.getElementById('clienteDestino').required = true;
  } else {
    campoClienteDestino.classList.add('d-none');
    document.getElementById('clienteDestino').required = false;
  }
}

// Función para buscar cliente para transacción programada
function buscarClienteProgramado(tipo) {
  const idCampo = tipo === 'origen' ? 'clienteOrigen' : 'clienteDestino';
  const cedula = document.getElementById(idCampo).value;
  
  if (!cedula) {
    showMessage(`Ingrese la cédula del cliente ${tipo}`, 'warning');
    return;
  }
  
  showMessage(`Buscando cliente ${tipo}...`, 'info');
  
  fetchAPI(`/api/clientes/document/${cedula}`)
    .then(cliente => {
      showMessage(`Cliente encontrado: ${cliente.nombre} ${cliente.apellido}`, 'success');
    })
    .catch(error => {
      showMessage(`Error al buscar cliente ${tipo}: ${error.message}`, 'danger');
      console.error(`Error al buscar cliente ${tipo}:`, error);
    });
}

// Función para resetear el formulario de transacción programada
function resetProgramarTransaccionForm() {
  // Reset form to clear all input values
  document.getElementById('programarTransaccionForm').reset();
  
  // Reset the tipo transaccion to default
  document.getElementById('tipoTransaccion').value = 'DEPOSITO';
  
  // Hide destination client field
  document.getElementById('campoClienteDestino').classList.add('d-none');
  document.getElementById('clienteDestino').required = false;
}

// Configurar evento para programar transacción
document.getElementById('programarTransaccionForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const tipoTransaccion = document.getElementById('tipoTransaccion').value;
  const monto = parseFloat(document.getElementById('monto').value);
  const fechaEjecucion = document.getElementById('fechaEjecucion').value;
  const periodicidad = document.getElementById('periodicidad').value;
  const clienteOrigen = document.getElementById('clienteOrigen').value;
  
  if (!clienteOrigen) {
    showMessage('Ingrese la cédula del cliente origen', 'warning');
    return;
  }
  
  if (!monto || monto <= 0) {
    showMessage('Ingrese un monto válido mayor a cero', 'warning');
    return;
  }
  
  if (!fechaEjecucion) {
    showMessage('Seleccione una fecha de ejecución', 'warning');
    return;
  }
  
  // Objeto base para todos los tipos de transacciones
  const programadaData = {
    clienteOrigen: clienteOrigen,
    monto: monto,
    tipoTransaccion: tipoTransaccion,
    fechaEjecucion: fechaEjecucion,
    periodicidad: periodicidad
  };
  
  // Si es transferencia, agregar el cliente destino
  if (tipoTransaccion === 'TRANSFERENCIA_SALIENTE') {
    const clienteDestino = document.getElementById('clienteDestino').value;
    
    if (!clienteDestino) {
      showMessage('Ingrese la cédula del cliente destino', 'warning');
      return;
    }
    
    programadaData.clienteDestino = clienteDestino;
  }
  
  // Mostrar indicador de carga y deshabilitar botón
  const btnSubmit = e.target.querySelector('button[type="submit"]');
  const btnText = btnSubmit.innerHTML;
  btnSubmit.disabled = true;
  btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...`;
  
  try {
    const response = await fetchAPI('/api/transacciones-programadas', {
      method: 'POST',
      body: JSON.stringify(programadaData)
    });
    
    // Extract ID from response
    const id = response?.id || response;
    
    showMessage(`Transacción programada exitosamente. ID: ${id}`, 'success');
    
    // Reset form and fields
    document.getElementById('programarTransaccionForm').reset();
    document.getElementById('campoClienteDestino').classList.add('d-none');
    
    // Actualizar lista si hay una consulta activa
    const cedulaClienteConsulta = document.getElementById('cedulaClienteConsulta');
    if (cedulaClienteConsulta && cedulaClienteConsulta.value) {
      document.getElementById('consultaProgramadasForm').dispatchEvent(new Event('submit'));
    }
  } catch (error) {
    console.error('Error al programar transacción:', error);
    showMessage(`Error al programar transacción: ${error.message}`, 'danger');
  } finally {
    btnSubmit.disabled = false;
    btnSubmit.innerHTML = btnText;
  }
});

async function cargarMonederosPorCliente(cedulaCliente) {
  try {
    const monederos = await fetchAPI(`/api/monederos/cliente/${cedulaCliente}`);
    const saldoTotal = await fetchAPI(`/api/monederos/cliente/${cedulaCliente}/saldo-total`);
    
    mostrarMonederos(monederos, saldoTotal.saldoTotal, cedulaCliente);
    mostrarGrafoRelaciones(cedulaCliente, monederos);
  } catch (error) {
    showMessage(`Error al cargar monederos: ${error.message}`, 'danger');
  }
}