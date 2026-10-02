Historias de Usuario — FinanzIQ

HU-001: Registro de Usuario Final

Título corto: Registrador cuenta de usuario final
Módulo: Autenticación y Cuentas
Prioridad: Alta
Estimación: 3 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: crear una cuenta en la aplicación ingresando mis datos personales (nombre, correo y contraseña)

Para: acceder de forma privada a mis herramientas de gestión financiera personal

Contexto y detalles de soporte

Descripción/Notas: Requiere encriptación de contraseña (Bcrypt/Argon2). Debe enviar correo de confirmación. Validar formato de correo electrónico y fortaleza de contraseña.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario no tiene una cuenta activa y está en la pantalla de registro
* Cuando: ingresa un correo válido, segura contraseña y presiona “Registrarse”
* Entonces: el sistema crea el usuario, envía correo de verificación y redirige al inicio de sesión con el mensaje “Cuenta creada con éxito”.

Escenario 2 

* Dado que: el usuario intenta registrarse con un correo electrónico que ya existe en la base de datos
* Cuando: presiona “Registrarse”
* Entonces: el sistema bloquea el registro y muestra la alerta “El correo electrónico ya está registrado”.


HU-002: Inicio de Sesión

Título corto: Iniciar sesión en la plataforma
Módulo: Autenticación y Cuentas
Prioridad: Alta
Estimación: 3 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: autenticarme con mi correo y contraseña

Para: acceder a mi información financiera guardada de manera segura

Contexto y detalles de soporte

Descripción/Notas: Generación de JWT (JSON Web Token) con tiempo de vencimiento. Permitir recuperación de contraseña vía correo.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario ingresa sus credenciales correctas
* Cuando: presiona “Iniciar Sesión”
* Entonces: el sistema responde con un token de sesión válida y muestra el panel principal (Dashboard).

Escenario 2 

* Dado que: el usuario ingresa una contraseña incorrecta tres veces seguidas
* Cuando: intenta presionar “Iniciar Sesión”
* Entonces: el sistema bloquea temporalmente el intento por 5 minutos y muestra “Credenciales incorrectas”.
  

HU-003: Registrador Ingresos y Gastos

Título corto: Registrador de movimientos de ingresos y gastos
Módulo: Gestión de Transacciones
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: ingresar transacciones de ingresos o egresos especificando monto en C$, fecha y categoría

Para: mantener actualizado el historial de mis movimientos monetarios

Contexto y detalles de soporte

Descripción/Notas: Validación de monto positivo. Actualización automática de saldos de cuentas asociadas.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario está en el formulario de registro de movimiento
* Cuando: selecciona “Gasto”, ingresa C$ 500, selecciona la categoría “Alimentación” y presiona “Guardar”
* Entonces: el movimiento se guarda en la BD, se descuenta de la cuenta seleccionada y se muestra en el historial.

Escenario 2 

* Dado que: el usuario intenta guardar un registro con un monto igual o menor a 0
* Cuando: presiona “Guardar”
* Entonces: el sistema invalida el formulario y muestra “El monto debe ser un valor positivo en C$”.



HU-004: Creación de Presupuestos Mensuales

Título corto: Crear y configurar presupuestos mensuales
Módulo: Presupuestos y Control
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: definir un límite de gasto mensual por categoría o global en C$

Para: llevar a cabo una planificación estricta de mis finanzas y evitar sobrecostos

Contexto y detalles de soporte

Descripción/Notas: Un presupuesto está ligado a un rango mensual (ej. mayo 2026). Permite asociar alertas cuando se alcanza el 80% o 100%.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario define un presupuesto de C$ 4,000 para “Entretenimiento”
* Cuando: confirma el presupuesto para el mes actual
* Entonces: el sistema almacena el objetivo y muestra una barra de progreso que compara los gastos reales frente al límite.

Escenario 2 

* Dado que: el usuario ya tiene un presupuesto activo para la categoría “Entretenimiento” en el mes actual
* Cuando: intenta crear otro presupuesto idéntico
* Entonces: el sistema le sugiere actualizar el existente en lugar de duplicarlo.



HU-005: Exportar Informes (PDF/Excel)

Título corto: Generar y descargar informes en PDF/Excel
Módulo: Reportes y Exportación
Prioridad: Medios
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: exportar mis reportes de ingresos, gastos y saldos en formato PDF o XLSX

Para: respaldar mi información o imprimir mis estados de cuenta personales

Contexto y detalles de soporte

Descripción/Notas: Uso de librerías como PDFKit/SheetJS. Filtrado previo por rango de fechas (mes/año).

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario selecciona el rango “Último Mes” y formato “PDF”
* Cuando: presiona “Generar Informe”
* Entonces: el backend procesa el documento y descarga una estructura .pdf archivada con la información correspondiente.

Escenario 2 

* Dado que: el usuario solicita un informe de un período donde no existen registros de movimientos
* Cuando: presiona “Generar Informe”
* Entonces: el sistema informa “No hay movimientos registrados para el rango seleccionado”.


HU-006: Agregar Categoría Personalizada

Título corto: Crear categorías de usuario personalizados
Módulo: Configuración de Categorías
Prioridad: Medios
Estimación: 3 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: añadir categorías de gastos o ingresos creados por mí mismo

Para: clasificar mis movimientos según mis hábitos específicos de consumo que no están en las categorías globales

Contexto y detalles de soporte

Descripción/Notas: Las categorías creadas por el usuario son de alcance privado (solo visibles para ese ID de usuario).

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario abre el gestor de categorías
* Cuando: crea una categoría llamada “Mascotas”, asigna un color y un ícono y presiona “Agregar”
* Entonces: la nueva categoría pasa a formar parte de su listado desplegable para futuras transacciones.

Escenario 2 

* Dado que: el usuario intenta crear una categoría personalizada con un nombre que ya utiliza
* Cuando: hace clic en “Agregar”
* Entonces: el sistema avisa “Ya posee una categoría con el nombre especificado”.



HU-007: Administrador de Cuentas Financieras

Título corto: Administrar cuentas y billeteras del usuario
Módulo: Gestión de Cuentas
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: crear, editar o desactivar mis cuentas (Efectivo, Tarjeta de Débito, Billetera Digital)

Para: reflejar exactamente la distribución real de mi dinero disponible

Contexto y detalles de soporte

Descripción/Notas: Cada cuenta posee un saldo inicial en C$. Desactivar una cuenta no elimina su historial de transacciones.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario desea registrar su “Billetera Virtual”
* Cuando: ingresa el nombre, tipo de cuenta y saldo inicial de C$ 1,200
* Entonces: el sistema crea la cuenta y la suma al saldo global reflejado en el panel principal.

Escenario 2 

* Dado que: una cuenta posee transacciones asociadas
* Cuando: el usuario intenta eliminarla
* Entonces: el sistema le impide borrarla directamente y le sugiere cambiar su estado a “Inactiva/Archivada”.



HU-008: Configurar Preferencias

Título corto: Configurar opciones de perfil y preferencias
Módulo: Perfil y Ajustes
Prioridad: Baja
Estimación: 2 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: personalizar mis preferencias (moneda predeterminada C$, modo oscuro/claro, notificaciones)

Para: adaptar la experiencia de uso a mis necesidades particulares

Contexto y detalles de soporte

Descripción/Notas: Los ajustes se guardan en las preferencias del perfil de la BD y se aplican en la UI.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario ingresa a la pantalla de preferencias
* Cuando: activa la opción “Modo Oscuro” y notificaciones por correo
* Entonces: la aplicación cambia de inmediato su tema visual y guarda las reglas de notificación.

Escenario 2 

* Dado que: no hay conexión a internet al modificar las preferencias
* Cuando: intenta guardar los cambios
* Entonces: se muestra el aviso “Sin conexión. Los ajustes se guardarán localmente hasta reconectar”.



HU-009: Transferir entre Cuentas

Título corto: Realizar transferencias internas entre cuentas
Módulo: Gestión de Transacciones
Prioridad: Medios
Estimación: 3 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: registrar la transferencia de un monto en C$ entre mis distintas cuentas

Para: mover fondos de una cuenta a otra (ej. retirar efectivo del banco) sin alterarlo como un egreso o ingreso global

Contexto y detalles de soporte

Descripción/Notas: Genera automáticamente dos movimientos vinculados: un débito en la cuenta origen y un crédito en la cuenta destino.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario selecciona origen “Cuenta Banco” y destino “Efectivo” por C$ 1,000
* Cuando: confirma la operación
* Entonces: el saldo del banco disminuye C$ 1,000, el saldo de efectivo aumenta C$ 1,000 y el saldo total del usuario se mantiene igual.

Escenario 2 

* Dado que: el usuario intenta transferir entre la misma cuenta origen y destino
* Cuando: presiona “Confirmar”
* Entonces: el sistema detiene la operación notificando “La cuenta de origen y destino deben ser distintos”.



HU-010: Establecer Metas de Ahorro

Título corto: Definir metas de ahorro personal
Módulo: Ahorro y Metas
Prioridad: Medios
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: fijar metas de ahorro asociando un monto objetivo en C$ y una fecha límite

Para: medir mi avance y motivación al juntar capital para compras o emergencias

Contexto y detalles de soporte

Descripción/Notas: Permite asignar depósitos periódicos a la meta desde el saldo de las cuentas activas.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario crea la meta “Fondo de Emergencia” con un objetivo de C$ 12,000 a 6 meses
* Cuando: guarda la configuración
* Entonces: el sistema calcula la cuota sugerida (C$ 2,000/mes) y muestra la barra de progreso general.

Escenario 2 

* Dado que: el usuario coloca una fecha límite que ya transcurrió en el calendario
* Cuando: presiona “Crear Meta”
* Entonces: el sistema rechaza la fecha informando “La fecha meta debe ser posterior a la fecha actual”.



HU-011: Simulaciones de Ahorro

Título corto: Escenarios similares de ahorro proyectado
Módulo: Ahorro y Metas
Prioridad: Medios
Estimación: 3 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: ingresar un monto mensual estimado y un período de tiempo

Para: simular y prever cuánto dinero lograré acumular (ej. “Si ahorras C$500 al mes, en un año tendrás C$6.000”)

Contexto y detalles de soporte

Descripción/Notas: Calculadora interactiva que opera de forma inmediata en el cliente o mediante llamadas sencillas de simulación.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario ingresa C$ 500 mensuales a un plazo de 12 meses
* Cuando: ejecuta la simulación
* Entonces: la pantalla calcula e informa “En 12 meses habrás acumulado un total de C$ 6,000”.

Escenario 2 

* Dado que: el usuario ingresa 0 meses de plazo
* Cuando: consulta el cálculo
* Entonces: el sistema solicita “Ingresa un número de meses mayor a cero para simular”.



HU-012: Recordatorio de Pagos

Título corto: Programar recordatorios de pagos recurrentes
Módulo: Recordatorios y Servicios
Prioridad: Medios
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: programar alertas de vencimiento para pagos de servicios (agua, luz, internet, préstamos)

Para: evitar recargos o cortes por morosidad en mis obligaciones

Contexto y detalles de soporte

Descripción/Notas: Sistema de notificaciones programadas (Push/Email). Posibilidad de marcar el servicio como “Pagado” y generar el gasto automáticamente.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario programa un recordatorio de “Servicio de Luz” por C$ 800 para los días 15 de cada mes
* Cuando: llega la fecha previa configurada (ej. día 13)
* Entonces: el sistema emite una notificación de alerta con el monto y fecha límite de pago.

Escenario 2 

* Dado que: el usuario marca un recordatorio pendiente como “Pagado”
* Cuando: confirma la acción
* Entonces: la alerta del mes se desactiva y el sistema pregunta si desea registrar la transacción como un egreso automático.



HU-013: Registrador de Cuentas de Entidades Financieras

Título corto: Conectar y registrar entidades financieras externas
Módulo: Integraciones Bancarias
Prioridad: Medios
Estimación: 5 puntos de historia

Redacción estándar

Como: Usuario final de FinanzIQ

Quiero: dar de alta mis cuentas bancarias asociadas a entidades financieras reconocidas

Para: consolidar la procedencia de mis fondos bajo los nombres de las instituciones reales del país

Contexto y detalles de soporte

Descripción/Notas: Selección de entidades bancarias desde un catálogo habilitado.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el usuario selecciona una entidad bancaria del catálogo oficial y escribe su número de cuenta abreviado
* Cuando: presiona “Vincular Cuenta”
* Entonces: la cuenta queda dada de alta en su perfil lista para recibir sincronizaciones o movimientos.

Escenario 2 

* Dado que: el usuario intenta registrar la misma cuenta de la entidad dos veces
* Cuando: da clic en “Vincular Cuenta”
* Entonces: el sistema advierte “La cuenta especificada ya se encuentra vinculada a su perfil”.
  

MÓDULO 2: ROL ADMINISTRADOR

HU-014: Administrador de Usuarios de la Plataforma

Título corto: Gestionar usuarios del sistema
Módulo: Administración y Seguridad
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: Administrador de FinanzIQ

Quiero: buscar, revisar el estado, bloquear o desbloquear cuentas de usuarios

Para: mantener la integridad de la plataforma y atender soporte o incumplimiento de condiciones

Contexto y detalles de soporte

Descripción/Notas: Requiere rol ROLE_ADMIN. El bloqueo invalida de inmediato los tokens del usuario.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el administrador busca a un usuario por correo en el panel
* Cuando: presiona “Suspender Cuenta” e ingresa el motivo
* Entonces: la cuenta pasa a estado “Suspendida” y se cierra la sesión en los dispositivos del usuario.

Escenario 2 

* Dado que: el administrador intenta eliminar su propia cuenta de administrador
* Cuando: intenta ejecutar la acción
* Entonces: el sistema bloquea el procedimiento indicando “No puede auto-suspender su cuenta activa de administración”.



HU-015: Acceso a Registros Históricos y Auditoría

Título corto: Consultar logs y registros históricos del sistema
Módulo: Auditoría y Monitoreo
Prioridad: Medios
Estimación: 5 puntos de historia

Redacción estándar

Como: Administrador de FinanzIQ

Quiero: consultar la bitácora de eventos, inicios de sesión y cambios de datos críticos del sistema

Para: rastrear actividades sospechosas, diagnosticar fallos y asegurar la auditoría del software

Contexto y detalles de soporte

Descripción/Notas: Bitácoras (logs) de lectura estructuradas por fecha, tipo de evento, IP y usuario.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el administrador ingresa a la sección de Auditoría
* Cuando: filtra los registros por la categoría “Inicios de sesión fallidos” en la última semana
* Entonces: el sistema expone el listado histórico detallado con marca de tiempo e IP de origen.

Escenario 2 

* Dado que: no existen registros para los filtros ingresados
* Cuando: aplica la búsqueda
* Entonces: se muestra el estado “No se encontraron eventos registrados en el período indicado”.
  

HU-016: Administrador Proveedores Disponibles

Título corto: Gestionar catálogo de proveedores e instituciones financieras
Módulo: Administración de Integraciones
Prioridad: Medios
Estimación: 3 puntos de historia

Redacción estándar

Como: Administrador de FinanzIQ

Quiero: dar de alta o actualizar la lista de proveedores (bancos, fintechs, procesadores) disponibles en la app

Para: mantener actualizado el catálogo de integraciones a las que tienen acceso los usuarios

Contexto y detalles de soporte

Descripción/Notas: Permite activar o desactivar la disponibilidad de un proveedor globalmente.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el administrador da de alta un nuevo proveedor financiero
* Cuando: completa el nombre, logo y marca el estado como “Activo”
* Entonces: la entidad financiera queda inmediatamente visible en el catálogo de los usuarios finales.

Escenario 2 

* Dado que: el administrador desactiva un proveedor por mantenimiento
* Cuando: confirma el cambio de estado
* Entonces: el sistema deshabilita la opción de vinculación para nuevos usuarios sin afectar las cuentas ya configuradas.


HU-017: Implementar Cambios en Sistema de Seguridad

Título corto: Configurar parámetros globales de seguridad
Módulo: Administración y Seguridad
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: Administrador de FinanzIQ

Quiero: ajustar directivas de seguridad (duración de tokens, número de intentos de inicio de sesión, requisitos de clave)

Para: blindar la plataforma ante vulnerabilidades y ataques informáticos

Contexto y detalles de soporte

Descripción/Notas: Cambios aplicados dinámicamente sobre la lógica del módulo de autenticación.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el administrador modifica el tiempo máximo de vencimiento del token a 60 minutos
* Cuando: guarda la nueva configuración
* Entonces: el sistema actualiza la directiva global y todos los nuevos tokens generados caducarán según el nuevo plazo.

Escenario 2 

* Dado que: se intenta establecer un parámetro de seguridad fuera del límite seguro (ej. 0 intentos de inicio de sesión)
* Cuando: intenta guardar
* Entonces: el sistema rechaza el valor indicando “Parámetro fuera del rango permitido”.
  

HU-018: Monitorear Funcionamiento de APIs Integradas

Título corto: Monitorear estado y rendimiento de APIs integradas
Módulo: Auditoría y Monitoreo
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: Administrador de FinanzIQ

Quiero: visualizar la latencia, tasa de errores (HTTP 4xx/5xx) y uso del servicio de las APIs

Para: detectar caídas de servicios integrados y garantizar la disponibilidad del sistema

Contexto y detalles de soporte

Descripción/Notas: Panel de telemetría de APIs que monitorea tiempos de respuesta y servicios de terceros.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el administrador ingresa al monitor de APIs
* Cuando: consulta el estado de las integraciones
* Entonces: el sistema muestra indicadores gráficos en verde/amarillo/rojo con la latencia promedio y porcentaje de tiempo de actividad.

Escenario 2 

* Dado que: la API de un proveedor supera una tasa de error del 15% en un período de 10 minutos
* Cuando: el monitor procesa la métrica
* Entonces: emite una alerta crítica visual notificando al administrador “Fallo detectado en API externa”

HU-019: Control de Versiones del Sistema

Título corto: Gestionar el historial de versiones del sistema.
Módulo: Administración del Sistema
Prioridad: Baja
Estimación: 3 puntos de historia

Redacción estándar

Como: Administrador de FinanzIQ

Quiero: registrar implementando versiones, notas de cambios (release notes) y forzar actualización de cliente si es necesario

Para: mantener a los usuarios informados y garantizar la compatibilidad con parches de software.

Contexto y detalles de soporte

Descripción/Notas: Registro de versión Semantic Versioning (ej. v1.2.0). Incompatibilidades de API devuelven aviso al cliente.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el administrador publica una nueva versión estable v2.0.0 marcándola como “Requerida”
* Cuando: un usuario abre una versión obsoleta en su dispositivo
* Entonces: el sistema despliega el mensaje “Existe una nueva versión obligatoria disponible. Actualice para continuar”.

Escenario 2 

* Dado que: la actualización publicada es menor (ej. v1.0.1 opcional)
* Cuando: el usuario accede
* Entonces: el sistema muestra la nota de actualización pero le permite omitirla y seguir usando la aplicación.


MÓDULO 3: ROL APIS E INTEGRACIONES DE SERVICIO

HU-020: Generar Recomendaciones Automáticas

Título corto: Servicio API de generación de recomendaciones financieras
Módulo: Servicios Inteligentes API
Prioridad: Alta
Estimación: 8 puntos de historia

Redacción estándar

Como: API del Motor Analítico de FinanzIQ

Quiero: procesar los gastos históricos del usuario y responder solicitudes con recomendaciones automatizadas

Para: enviar sugerencias personalizadas de ahorro (ej. “Este mes gastaste un 30 % más en transporte”)

Contexto y detalles de soporte

Descripción/Notas: Punto final REST (/api/v1/analytics/recommendations). Recibe token del usuario y analiza variaciones en los gastos del mes contra el promedio histórico.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: la API recibe una solicitud GET autenticada para un usuario con variaciones de gasto elevadas
* Cuando: ejecuta el análisis comparativo
* Entonces: responde con estado HTTP 200 OK y un cuerpo JSON con la lista de textos estructurados de recomendación en C$.

Escenario 2 

* Dado que: el usuario no posee registros suficientes para realizar un cálculo estadístico significativo
* Cuando: la API procesa la petición
* Entonces: responde HTTP 200 OK con un listado con mensajes educativos predeterminados.
  

HU-021: Proveer Datos Estructurados para Gráficas

Título corto: Servicio API de agregación de datos para gráficos
Módulo: Servicios Inteligentes API
Prioridad: Alta
Estimación: 5 puntos de historia

Redacción estándar

Como: API de Métricas y Visualización de FinanzIQ

Quiero: agregar y calcular totales de ingresos vs. gastos por categorías, semanas o meses

Para: proporcionar al Frontend los datos requeridos para renderizar gráficos financieros

Contexto y detalles de soporte

Descripción/Notas: Punto final /api/v1/charts/balance. Devuelve estructuras optimizadas en JSON para librerías gráficas (Chart.js / Flott).

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: el Frontend solicita los datos para la gráfica “Ingresos vs Gastos” del año en curso
* Cuando: la API procesa la consulta agregada en la BD
* Entonces: retorna HTTP 200 OK con arreglos ordenados por meses con los totales equivalentes.

Escenario 2 

* Dado que: el rango de fechas solicitado en los parámetros de la API es inválido o invertido
* Cuando: la API recibe la petición
* Entonces: responde HTTP 400 Bad Request indicando “Rango de fechas erróneo”.
  

HU-022: Servicio del Asistente Financiero en Lenguaje Natural

Título corto: Endpoint del Asistente Virtual con IA
Módulo: Servicios Inteligentes API
Prioridad: Alta
Estimación: 8 puntos de historia

Redacción estándar

Como: API del Asistente Financiero IA de FinanzIQ

Quiero: recibir preguntas en lenguaje natural expresadas por el usuario

Para: consultar el contexto del usuario y responder inquietudes sobre sus finanzas (ej. “¿En qué gasté más este mes?”)

Contexto y detalles de soporte

Descripción/Notas: Integra un LLM mediante un aviso que sintetiza la información del usuario en C$. Autenticación estricta por token.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: la API recibe en /api/v1/ai/assistant el aviso “¿Cuánto puedo gastar esta semana?”
* Cuando: cruza el presupuesto activo frente al consumo real
* Entonces: responde HTTP 200 OK con un mensaje claro recomendando el límite de gasto sugerido en C$.

Escenario 2 

* Dado que: la petición enviada a la API no contiene el token JWT de autenticación
* Cuando: la API recibe la llamada
* Entonces: bloquea la consulta y retorna HTTP 401 Unauthorized.


HU-023: Motor API de Simulaciones de Ahorro y Proyección

Título corto: Servicio API de cálculos y proyecciones de ahorro
Módulo: Servicios Inteligentes API
Prioridad: Medios
Estimación: 5 puntos de historia

Redacción estándar

Como: API del Motor de Simulaciones de FinanzIQ

Quiero: procesar los parámetros de aporte periódico, tiempo y rendimiento

Para: calcular modelos matemáticos de crecimiento financiero que alimentan los simuladores de la aplicación

Contexto y detalles de soporte

Descripción/Notas: Punto final /api/v1/simulations/calculate. Devuelve proyecciones mes a mes del comportamiento del capital.

Criterios de aceptación (BDD)

Escenario 1 

* Dado que: la API recibe un JSON con aportes de C$ 500 por 12 meses
* Cuando: la API ejecuta las fórmulas de proyección
* Entonces: retorna HTTP 200 OK junto con el desglose acumulado de capital resultante de C$ 6,000.

Escenario 2 

* Dado que: los parámetros enviados incluyen valores nulos o cadenas de texto no válidas en los montos
* Cuando: se llama a la API
* Entonces: responde HTTP 422 Unprocessable Entity señalando los parámetros requeridos.
