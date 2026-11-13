# Manual de Usuario — FinanzIQ

**Versión de referencia:** 2.0.0  
**Plataforma:** Android (Kotlin / Jetpack Compose)  
**Subtítulo oficial:** *Tus Finanzas, Inteligencia y Control*

---

## Tabla de Contenidos

1. [Información General de FinanzIQ](#1-información-general-de-finanziq)
2. [Acceso, Registro y Seguridad de Cuenta](#2-acceso-registro-y-seguridad-de-cuenta)
3. [Estructura de Navegación y Barra Superior](#3-estructura-de-navegación-y-barra-superior)
4. [Interfaz de Inicio (Dashboard Principal)](#4-interfaz-de-inicio-dashboard-principal)
5. [Interfaz de Billeteras y Cuentas Bancarias](#5-interfaz-de-billeteras-y-cuentas-bancarias)
6. [Interfaz de Movimientos y Transferencias](#6-interfaz-de-movimientos-y-transferencias)
7. [Interfaz de Presupuestos Mensuales](#7-interfaz-de-presupuestos-mensuales)
8. [Interfaz de Análisis Financiero y Reportes PDF](#8-interfaz-de-análisis-financiero-y-reportes-pdf)
9. [Interfaz de Metas de Ahorro y Simulador](#9-interfaz-de-metas-de-ahorro-y-simulador)
10. [Interfaz de Pagos y Recordatorios de Facturas](#10-interfaz-de-pagos-y-recordatorios-de-facturas)
11. [Interfaz del Asistente de IA Financiero](#11-interfaz-del-asistente-de-ia-financiero)
12. [Ajustes, Preferencias Globales y Control de Actualizaciones](#12-ajustes-preferencias-globales-y-control-de-actualizaciones)
13. [Interfaz de Administración (Exclusivo Rol Administrador)](#13-interfaz-de-administración-exclusivo-rol-administrador)
14. [Resumen de Reglas de Validación, Límites y Solución de Problemas](#14-resumen-de-reglas-de-validación-límites-y-solución-de-problemas)

---

## 1. Información General de FinanzIQ

**FinanzIQ** es una aplicación móvil de gestión financiera personal diseñada para registrar ingresos y gastos, vincular cuentas bancarias y billeteras digitales, controlar presupuestos mensuales, proyectar ahorros, programar recordatorios de pagos, generar reportes financieros en PDF y recibir orientación analítica con un Asistente de IA integrado.

### 1.1 Perfiles de Usuario (Roles)

La aplicación distingue dos roles con alcances específicos:

| Rol | Identificador en Cabecera | Alcance y Funciones |
| :--- | :--- | :--- |
| **Usuario Final** (`USER`) | `👤 Usuario` / `👤 Customer` | Acceso completo a sus finanzas personales: Inicio, Billeteras y Cuentas, Movimientos, Presupuestos, Análisis, Metas y Simulador de Ahorro, Pagos/Facturas, Asistente de IA, Reportes PDF y Ajustes de preferencia. |
| **Administrador** (`ADMIN`) | `🛡️ Administrador` / `🛡️ Admin` | Incluye todas las herramientas financieras del Usuario Final **más** la **Interfaz de Administración** con 7 módulos de control: Entidades Financieras, Usuarios, Auditoría, Monitor de APIs, Versiones, Directivas de Seguridad y Categorías. |

### 1.2 Moneda Base y Conversión Dinámica

- La aplicación almacena internamente los registros sobre moneda base **NIO (Córdoba Nicaragüense - `C$`)** y realiza conversiones automáticas en pantalla según la moneda preferida por el usuario (27 divisas americanas y europeas soportadas, incluyendo `NIO`, `USD`, `EUR`, entre otras), aplicando tipos de cambio oficiales configurados (por ejemplo, `1 USD = 36.6243 NIO`, `1 EUR = 39.85 NIO`).
- Todas las interfaces admiten traducción global inmediata entre **Español (`ES`)** e **Inglés (`EN`)** y cambio dinámico entre **Modo Claro** y **Modo Oscuro**.

---

## 2. Acceso, Registro y Seguridad de Cuenta

### 2.1 Interfaz de Inicio de Sesión

Al abrir FinanzIQ se despliega la **Interfaz de Inicio de Sesión**, la cual contiene:

- **Selector superior de Tema (`Oscuro` / `Claro`)**: permite alternar la apariencia visual antes de autenticarse.
- **Selector superior de Idioma (`🇪🇸 ES` / `🇺🇸 EN`)**: cambia globalmente el idioma de toda la aplicación desde la pantalla de acceso.
- **Campos de acceso:**
  - **Usuario o Correo electrónico:** acepta tanto el nombre de usuario (ej. `elena.gomez`) como el correo registrado (ej. `elena@finanzas.com`).
  - **Contraseña:** campo oculto con botón de visibilidad (ojo) para mostrar u ocultar los caracteres.
- **Botón `Iniciar Sesión`:** valida credenciales contra la base de datos local y las directivas de seguridad vigentes.
- **Enlace `¿Olvidaste tu contraseña?`:** abre un formulario para validar un correo electrónico con formato válido (`usuario@dominio.com`) y verificar si existe en la plataforma para gestionar la recuperación.
- **Enlace de registro:** permite navegar hacia la **Interfaz de Registro de Usuario**.

#### Cuentas de Acceso Rápido de Prueba

En la parte inferior de la **Interfaz de Inicio de Sesión**, en el bloque **Acceso Rápido de Prueba**, se incluyen dos botones para probar todos los flujos sin necesidad de crear una cuenta manual:

| Botón | Rol | Nombre Completo | Usuario | Correo | Contraseña | Teléfono |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`Elena (Usuario)`** | `USER` | Elena Gómez | `elena.gomez` | `elena@finanzas.com` | `Elena2026!` | `77889900` |
| **`Carlos (Admin)`** | `ADMIN` | Carlos Mendoza | `admin` | `admin@finanzas.com` | `Admin123!` | `88997766` |

> **Nota sobre datos de prueba:** La cuenta de Elena inicia con un portafolio precargado (cuentas en BAC y LAFISE, tarjeta de crédito Banpro, billeteras móviles, movimientos recientes, presupuestos, metas y facturas pendientes). La cuenta de Carlos inicia con cuentas propias (Ficohsa, Avanz, BDF y Billetera Ficohsa) y acceso completo al módulo administrativo.

#### Bloqueo Temporal por Intentos Fallidos y Suspensión

1. **Bloqueo temporal:** Si se introducen credenciales incorrectas de forma consecutiva hasta alcanzar el máximo configurado en las directivas de seguridad (por defecto **3 intentos**), la aplicación bloquea temporalmente el inicio de sesión durante el tiempo configurado (por defecto **300 segundos / 5 minutos**) y muestra un contador regresivo en vivo en formato `MM:SS`.
2. **Cuenta suspendida:** Si un Administrador ha suspendido la cuenta de un usuario, al intentar iniciar sesión se impedirá el acceso y se mostrará la razón registrada por el administrador.

---

### 2.2 Interfaz de Registro de Usuario

Desde la **Interfaz de Inicio de Sesión**, al pulsar para crear cuenta se abre la **Interfaz de Registro de Usuario**. Todos los campos muestran retroalimentación inmediata según las **Directivas de Seguridad** configuradas por el Administrador:

| Campo | Reglas por Defecto (Configurables por el Admin) |
| :--- | :--- |
| **Nombres** | Obligatorio. Máximo **70 caracteres**. Si está activa la regla de capitalización, cada palabra debe iniciar con letra mayúscula (ej. `María José`). |
| **Apellidos** | Obligatorio. Máximo **70 caracteres**. Requiere iniciar con letra mayúscula por palabra según política vigente. |
| **Nombre de Usuario** | Entre **3 y 30 caracteres**. Solo letras, números y guion bajo (`_`). Debe ser único en la plataforma. |
| **Edad** | Numérico. Debe cumplir con la edad mínima vigente (por defecto **18 años**). |
| **Prefijo y Número de Teléfono** | Selector con **32 países de América y el Caribe** (ej. Nicaragua `+505`, Estados Unidos `+1`, México `+52`, Costa Rica `+506`, etc.). El número se valida automáticamente según la longitud y dígitos iniciales permitidos para el país elegido (ej. Nicaragua: **8 dígitos** iniciando con `5`, `7` u `8`). No puede repetirse con otro usuario. |
| **Moneda de Preferencia** | Lista de **27 divisas** (ej. `C$ - NIO`, `$ - USD`, `€ - EUR`, etc.) que define la moneda visual inicial del perfil. |
| **Correo Electrónico** | Formato estándar obligatorio (`usuario@dominio.com`). Debe ser único. |
| **Contraseña** | Longitud por defecto entre **8 y 20 caracteres**. Requiere por defecto al menos **1 mayúscula**, **1 número** y **1 carácter especial** (`@#$%&*!?` u otros símbolos). La exigencia de minúscula es configurable por el Admin. |
| **Confirmar Contraseña** | Debe coincidir exactamente con el campo de contraseña. |
| **Rol de Cuenta** | Permite elegir entre **Usuario** o **Administrador**. |

Si algún campo incumple las reglas o ya existe en el sistema, se mostrará un aviso rojo en la cabecera (*"No se pudo completar el registro. Verifica tus datos e intenta de nuevo"*) junto con el detalle del error en el campo correspondiente.

---

## 3. Estructura de Navegación y Barra Superior

Una vez iniciada la sesión, todas las interfaces principales comparten la siguiente estructura:

### 3.1 Barra Superior (Cabecera)

- **Lado izquierdo:** Ícono de FinanzIQ, nombre **FinanzIQ**, nombre del usuario conectado y su etiqueta de rol (`👤 Usuario` o `🛡️ Administrador`).
- **Lado derecho:** Avatar circular con la inicial del nombre del usuario. Al tocarlo se despliega un menú con dos opciones directas:
  - **Ajustes y Preferencias**
  - **Cerrar Sesión**

### 3.2 Barra de Navegación Inferior

Diseñada con una altura compacta (`56 dp`) para maximizar el área útil de pantalla. Contiene los siguientes accesos directos:

1. **Inicio** (`Dashboard`)
2. **Movimientos** (`Transactions`)
3. **Presupuestos** (`Budgets`)
4. **Análisis** (`Analytics`)
5. **Ahorros** (`Savings`)
6. **Pagos** (`Bills`)
7. **Asistente IA** (`AiAssistant`)
8. **Admin** *(visible únicamente si el usuario tiene rol `ADMIN`)*

> **Acceso a Billeteras y Cuentas:** La **Interfaz de Billeteras y Cuentas** no ocupa un botón en la barra inferior; se accede tocando directamente la **Tarjeta Principal de Balance (Hero Card)** dentro de la **Interfaz de Inicio**.

### 3.3 Botón Atrás del Sistema

En cualquier interfaz secundaria (Movimientos, Presupuestos, Análisis, Ahorros, Pagos, Asistente IA o Admin), al presionar el botón físico o gesto **Atrás** de Android, la aplicación regresa automáticamente a la **Interfaz de Inicio**. Desde Billeteras o el formulario de Gestión de Herramientas Financieras, vuelve a la interfaz anterior.

---

## 4. Interfaz de Inicio (Dashboard Principal)

Es la pantalla de bienvenida tras iniciar sesión y ofrece un resumen ejecutivo en tiempo real:

1. **Saludo y Acceso a Perfil:** Muestra saludo personalizado con el nombre del usuario y su avatar interactivo (al tocarlo abre **Ajustes y Preferencias**).
2. **Tarjeta Principal de Balance (Hero Card):**
   - Muestra el **Balance Consolidado de Billeteras y Cuentas Activas** convertido a la moneda seleccionada por el usuario (si el usuario aún no tiene cuentas activas registradas, muestra el balance neto calculado por ingresos menos gastos).
   - Muestra el mes y año actual, además de píldoras resumidas con el total de **Ingresos** y **Gastos**.
   - **Acción:** Al tocar esta tarjeta se abre la **Interfaz de Billeteras y Cuentas**.
3. **Tarjeta de Insight de IA:** Resumen rápido con acceso directo al tocar hacia la **Interfaz del Asistente de IA**.
4. **Acciones Rápidas (4 botones directos):**
   - **`+ Ingreso`:** Abre el formulario para registrar un nuevo ingreso.
   - **`+ Gasto`:** Abre el formulario para registrar un nuevo gasto.
   - **`Reporte PDF`:** Abre el generador de reporte financiero PDF por rango de fechas.
   - **`Simulador`:** Navega directamente a la **Interfaz de Metas de Ahorro y Simulador**.
5. **Tarjetas de Estado Financiero:**
   - **Metas de Ahorro:** Avance resumido de las metas activas.
   - **Predicción de Gasto / Tasa de Ahorro:** Indicadores calculados a partir de los movimientos del periodo.
   - **Alerta de Pagos Pendientes:** Banner informativo cuando existen facturas próximas a vencer.
6. **Movimientos Recientes:** Lista los últimos **5 movimientos** registrados (traducidos al idioma activo si corresponden a categorías/títulos del sistema), permitiendo identificar rápidamente tipo, monto y categoría.

---

## 5. Interfaz de Billeteras y Cuentas Bancarias

Se accede tocando la **Tarjeta Principal de Balance** en la **Interfaz de Inicio**.

### 5.1 Elementos Principales

- **Barra superior:** Botón de regreso, título de la interfaz y botón de **Transferencias** (abre el diálogo de transferencia entre cuentas).
- **Tarjeta de Resumen:** Muestra el **Balance Total Activo** y el **Límite de Crédito Total** de todas las cuentas y billeteras activas.
- **Aviso del Administrador:** Si el Administrador deshabilitó temporalmente un banco o proveedor de billetera, aparece un banner indicando las instituciones inactivas (no se podrán crear nuevas vinculaciones con ellas, aunque las cuentas ya existentes conservan su historial).
- **Pestañas de Filtro:** `Todas` | `Cuentas` | `Billeteras`.
- **Botón flotante (`+`):** Abre la **Interfaz de Registro de Herramienta Financiera**.

### 5.2 Registrar Cuenta Bancaria o Billetera Digital

Al pulsar `+` se abre el formulario de registro con dos tipos posibles:

#### Opción A: Cuenta Bancaria (`CUENTA`)

1. **Banco:** Lista desplegable únicamente con los bancos activos en el catálogo del Administrador (ej. `BANPRO`, `BAC`, `LAFISE`, `BDF`, `FICOHSA`, `AVANZ`).
2. **Tipo de Cuenta:**
   - **Débito (`DEBITO`):** Requiere seleccionar subtipo y cumplir el saldo mínimo de apertura:
     - `AHORRO`: mínimo **`C$ 500.00`**
     - `CORRIENTE`: mínimo **`C$ 2,500.00`**
     - `NOMINA`: mínimo **`C$ 0.00`** (sin mínimo)
   - **Crédito (`CREDITO`):**
     - Límite de crédito mínimo: **`C$ 7,000.00`**
     - Crédito disponible inicial: debe ser mayor o igual a `0` y no superar el límite de crédito.
3. **Número de Cuenta:** Debe tener **al menos 6 dígitos** y no puede estar duplicado ni en el mismo banco ni en otra institución registrada.

#### Opción B: Billetera Digital (`BILLETERA`)

1. **Proveedor de Billetera:** Lista únicamente billeteras habilitadas por el Administrador (ej. `Banpro Móvil`, `LAFISE Móvil`, `Kash BAC`, `BDF Móvil`, `Ficohsa Móvil`).
2. **Teléfono Asociado:** Debe coincidir exactamente con el número telefónico del perfil del usuario conectado y no puede repetirse en otra billetera del mismo proveedor.
3. **Cuenta Bancaria Vinculada:** Solo permite seleccionar una cuenta bancaria **activa** del usuario que pertenezca a la **misma marca bancaria** de la billetera (por ejemplo, `Kash BAC` solo puede vincularse con una cuenta `BAC`).
4. **Saldo Inicial:** Debe ser mayor o igual a `0`.

### 5.3 Deshabilitar, Reactivar y Eliminar Cuentas o Billeteras

- **Deshabilitar (botón rojo `✕` en tarjeta activa):** Solicita confirmación mediante un diálogo. Al confirmar, la cuenta pasa a estado inactivo: no recibe cargos ni aparece en selectores de nuevos movimientos o transferencias, pero conserva su historial.
- **Reactivar (botón verde `Reactivar` en tarjeta deshabilitada):** Las tarjetas inactivas se muestran atenuadas y permiten restaurar la disponibilidad operativa de la cuenta o billetera con un toque.
- **Eliminar definitivamente (ícono de papelera rojo en tarjeta deshabilitada):** Despliega un formulario de confirmación de borrado advirtiendo que la acción es definitiva y afectará los datos del balance general. Incluye botones **`Cancelar`** y **`Confirmar`**.

---

## 6. Interfaz de Movimientos y Transferencias

Permite consultar, filtrar, registrar y eliminar ingresos y gastos, además de ejecutar transferencias entre cuentas propias.

### 6.1 Búsqueda y Filtros

- **Barra de búsqueda:** Filtra en tiempo real por `concepto/título`, `categoría`, `grupo`, `subcategoría`, `destino` o `notas`.
- **Filtro por Periodo:** `Semana` | `Mes` | `Año` | `Todos`.
- **Filtro por Categoría:** Chips dinámicos generados a partir de las categorías del sistema y las categorías personalizadas creadas por el Administrador.
- **Filtro por Tipo:** `Todos` | `Ingresos` | `Gastos`.
- **Botones en Barra Superior:**
  - **Transferencias:** Abre el diálogo de transferencia entre cuentas.
  - **Reporte PDF:** Abre el generador de reporte PDF.

### 6.2 Registrar Nuevo Movimiento (Botón `+`)

Al pulsar `+` se despliega el formulario **`Registrar Movimiento`** con selector entre **Ingreso** y **Gasto**:

#### Registrar Ingreso (`INCOME`)

1. **Concepto / Título** y **Monto** (en la moneda actual del usuario).
2. **Subtipo de Ingreso:**
   - **Ingreso Activo** *(sueldo, salario, honorarios, trabajo directo)*
   - **Ingreso Pasivo** *(rentas, dividendos, regalías, rendimientos)*
   - Incluye texto informativo explicativo de cada modalidad.
3. **Categoría** (ej. `Salario`, `Negocio Propio`, `Inversiones`, `Otros Ingresos` o categorías personalizadas de ingreso creadas por el Admin).
4. **Frecuencia** (ej. único, semanal, quincenal, mensual) y **Fecha de pago**.
5. **Cuenta o Billetera de Destino:** Al guardar, el monto se **suma** automáticamente al saldo de la cuenta seleccionada y al balance general del Dashboard.

#### Registrar Gasto (`EXPENSE`)

1. **Concepto / Título** y **Monto**.
2. **Clasificación Jerárquica:**
   - **Grupo de Gasto:** `Gastos Fijos`, `Gastos Variables` o `Pago de Deudas`.
   - **Categoría** (incluye las predeterminadas y cualquier categoría creada por el Administrador dentro de ese grupo) y **Subcategoría**.
3. **Cuenta o Billetera de Origen:** Al guardar, verifica disponibilidad/selección y **resta** el monto del saldo de la cuenta elegida.
4. **Notas opcionales** y **Fecha**.

#### Reglas de Topes por Monto en Dólares Estadounidenses (USD)

Tanto en **Ingresos** como en **Gastos**, FinanzIQ convierte internamente el importe ingresado a su equivalente en **USD** antes de confirmar el guardado y aplica tres niveles de control:

| Equivalente en USD | Comportamiento del Sistema |
| :--- | :--- |
| **Menor a `$10,000 USD`** | Se registra con normalidad de forma inmediata. |
| **Entre `$10,000 USD` y `$1,000,000 USD`** | Permite guardar, pero despliega un aviso intermedio de confirmación: *"Estás registrando un monto inusualmente alto. ¿Deseas continuar?"* |
| **Mayor a `$1,000,000 USD`** | Bloquea el guardado automático y exige confirmación explícita escribiendo la palabra **`ACEPTAR`** en el cuadro de texto de seguridad para evitar errores de digitación. |

### 6.3 Eliminar un Movimiento

1. Al tocar el ícono de eliminar en un registro del historial, se despliega obligatoriamente el **Formulario de Confirmación** advirtiendo al usuario que la eliminación **afectará los datos de su balance general y de la cuenta asociada**, con los botones **`Cancelar`** y **`Confirmar`**.
2. Al confirmar:
   - Si el movimiento era un **Ingreso (`INCOME`)**, el monto se **resta** de la cuenta asociada y del balance general.
   - Si el movimiento era un **Gasto (`EXPENSE`)**, el monto se **devuelve (suma)** a la cuenta asociada y al balance general.

### 6.4 Transferencias entre Cuentas y Billeteras

Accesible desde la **Interfaz de Billeteras y Cuentas** o desde el botón superior en la **Interfaz de Movimientos**:

1. **Cuenta/Billetera de Origen** y **Cuenta/Billetera de Destino:** Solo muestra herramientas activas del usuario y exige que origen y destino sean diferentes.
2. **Validación de fondos y límites operativos:**
   - El monto debe ser mayor a `0`.
   - **Límite máximo si participa una Billetera Digital:** hasta **`C$ 15,000.00`**.
   - **Límite máximo entre Cuentas Bancarias:** hasta **`C$ 200,000.00`**.
3. **Comisión Interbancaria Automática:**
   - **Mismo banco:** comisión **`C$ 0.00`**.
   - **Bancos distintos:** comisión automática de **`C$ 80.00`**. El origen debe contar con saldo suficiente para cubrir `monto + comisión`; la aplicación descuenta ambos valores, acredita el monto en el destino y registra la comisión como un movimiento de gasto automático.

---

## 7. Interfaz de Presupuestos Mensuales

Permite establecer techos de gasto mensuales por categoría y monitorear el cumplimiento en tiempo real.

### 7.1 Resumen Global y Alertas Visuales

- **Tarjeta de Presupuesto General del Mes:** Suma todos los límites configurados frente al total gastado en esas categorías, mostrando barra de progreso adaptada para cantidades grandes (sin desbordes ni saltos de línea) y estado dinámico:
  - **Saludable:** consumo menor al `80%`.
  - **Alerta 80%:** consumo entre `80%` y `99%` (despliega además un banner de advertencia).
  - **Superado:** consumo igual o mayor al `100%`.

### 7.2 Crear, Editar y Eliminar Presupuestos

- **Crear Presupuesto (botón `+`):** Selecciona el grupo de gasto (`Fijos`, `Deseos`, `Deudas`), elige la categoría (incluidas categorías personalizadas del Admin) e ingresa el límite mensual.
- **Editar Límite (al tocar una tarjeta):** Al dar **tap sobre cualquier tarjeta de presupuesto existente**, se abre automáticamente el formulario **Editar Límite de Presupuesto** para ajustar el monto sin tener que borrarlo y recrearlo.
- **Eliminar Presupuesto (ícono de papelera):** Despliega el formulario de confirmación indicando el impacto en el control mensual, con botones **`Cancelar`** y **`Confirmar`**.

---

## 8. Interfaz de Análisis Financiero y Reportes PDF

### 8.1 Análisis Visual por Periodo

- **Selector de Periodo:** `Semanal` | `Mensual` | `Anual`.
- **Resumen del Periodo:** Muestra insignia de **Superávit** o **Déficit**, barra proporcional comparativa entre **Ingresos** y **Gastos**, totales monetarios y **Balance Neto**.
- **Gráfico de Barras Anidadas Verticales:**
  - Permite filtrar los gastos por grupo: `Todos`, `Gastos Fijos`, `Gastos Variables` o `Pago de Deudas`.
  - Las barras están adaptadas para cantidades grandes manteniendo alineación limpia.
  - **Interacción:** Al tocar la barra de una categoría, se despliega debajo un **mini-gráfico con el desglose de sus subcategorías** y tarjetas desplegables con el detalle monetario y porcentual.

### 8.2 Exportar Reporte Financiero en PDF

Puede abrirse desde la tarjeta destacada/botón superior en la **Interfaz de Análisis**, desde **Inicio** o desde **Movimientos**:

1. **Selección de Rango de Fechas:**
   - Chips rápidos: `Este Mes`, `Mes Pasado`, `Este Año`.
   - Selectores manuales de **Fecha Inicial** y **Fecha Final** mediante calendario nativo (`DatePickerDialog`).
2. **Validaciones:**
   - La fecha final debe ser igual o posterior a la fecha inicial.
   - Si no existen registros en el rango elegido, la aplicación muestra un mensaje de error y evita generar un archivo vacío.
3. **Documento PDF Generado:**
   - Genera un archivo **A4 real** con encabezado corporativo de FinanzIQ, datos del titular, periodo evaluado y **2 tablas estructuradas**:
     1. **Balance General (Ingresos y Gastos)** del periodo con totales y balance neto.
     2. **Desglose por Billeteras y Cuentas Vinculadas** (institución, tipo, número/referencia, estado y saldo actual).
   - Al finalizar, ofrece abrir o compartir el PDF directamente mediante el visor del dispositivo.

---

## 9. Interfaz de Metas de Ahorro y Simulador

Integra dos herramientas complementarias en una misma interfaz: el **Simulador de Ahorro** y la gestión de **Metas de Ahorro**.

### 9.1 Simulador de Ahorro (2 Modos)

Permite proyectar cuánto dinero acumulará el usuario antes de comprometer una meta:

1. **Modo Mensual:**
   - Campo numérico o deslizador para el ahorro mensual (`100` a `10,000` en base) con chips rápidos (`200`, `500`, `1,000`, `2,500`, `5,000`).
   - Chips de horizonte temporal: `6 meses`, `1 año`, `2 años`, `3 años`.
   - Calcula automáticamente el **ahorro proyectado (`monto × meses`)**.
2. **Modo Diario:**
   - Campo numérico o deslizador para el ahorro diario (`10` a `1,000` en base) con chips rápidos (`10`, `25`, `50`, `100`, `250`, `500`).
   - Campo o deslizador de plazo en días (`1` a `365`) con chips rápidos (`7d`, `15d`, `30d`, `60d`, `90d`, `180d`, `365d`).
   - Valida que tanto el monto diario como los días sean mayores a `0`, muestra el rango exacto de fechas proyectado y actualiza el total acumulado en tiempo real.

### 9.2 Metas de Ahorro (`En Progreso` vs `Alcanzadas`)

- **Crear Meta (botón `+`):** Solicita título de la meta, monto objetivo y aporte inicial opcional.
- **Clasificación automática en dos secciones:**
  - **En Progreso:** Metas cuyo monto ahorrado es menor al objetivo (`currentAmount < targetAmount`).
  - **🏆 Alcanzadas:** Metas que alcanzaron o superaron el `100%` del objetivo (`currentAmount >= targetAmount`).
- **Abonar a una Meta:**
  - Abre el modal de aporte donde se selecciona la **cuenta o billetera activa de origen** y el monto a aportar.
  - Valida que la cuenta tenga saldo disponible suficiente, descuenta el monto de la cuenta, lo suma a la meta y registra automáticamente un movimiento `"Aporte a Meta: <Título>"`.
  - **Mensaje de Felicitación:** Si el abono hace que la meta alcance o supere el `100%`, se despliega el diálogo **"🎉 ¡Meta Alcanzada con Éxito!"** y la meta se mueve automáticamente al apartado **Alcanzadas**.
- **Eliminar Metas (en progreso o alcanzadas):** Cualquier meta, incluida una meta cumplida en **Alcanzadas**, dispone de ícono de eliminación que abre el formulario de confirmación advirtiendo el impacto correspondiente con botones **`Cancelar`** y **`Confirmar`**.

---

## 10. Interfaz de Pagos y Recordatorios de Facturas

Permite programar compromisos financieros futuros y ejecutar su pago vinculándolos con cuentas reales.

### 10.1 Programar Recordatorio (Botón `+`)

- Solicita **Título** de la factura/pago, **Monto** (mayor a `0`), **Categoría** y **Fecha de vencimiento**.
- **Validación de fecha:** La fecha de vencimiento debe ser estrictamente **posterior al día actual**.
- **Alertas automáticas:** Si se registra una factura con vencimiento dentro de las próximas **48 horas (<= 2 días)**, se despliega inmediatamente una alerta modal informativa; asimismo, en la cabecera de la interfaz y en el Dashboard aparecen banners de **Urgente / Próximo a vencer** cuando faltan `<= 1` día o `48h`.

### 10.2 Flujo de Pago y Eliminación

Las facturas se agrupan en **`Por Pagar`** y **`Pagados`**:

1. **Marcar como Pagada:** Al tocar el botón o casilla de pago sobre una factura en `Por Pagar`, se abre el diálogo de pago con:
   - Descripción fija bloqueada **`Pago de Factura`** (`Bill Payment` en inglés).
   - Selector de **Cuenta o Billetera Activa de Origen**.
   - Selector de **Categoría de Gasto**.
   - Al confirmar, verifica saldo suficiente, descuenta el importe de la cuenta, crea el movimiento de gasto automático y mueve la factura al apartado **`Pagados`**.
2. **Eliminar Recordatorio:** El ícono de papelera abre el formulario de confirmación con **`Cancelar`** y **`Confirmar`**.

---

## 11. Interfaz del Asistente de IA Financiero

Ofrece orientación financiera conversacional y análisis gráfico/tabular generado directamente a partir de los datos reales y exclusivos del usuario conectado.

### 11.1 Consultas Analíticas (Gráficos y Tablas Automáticas)

El motor analítico integrado detecta palabras clave de análisis dentro de la pregunta del usuario (por ejemplo, chips sugeridos o consultas sobre ingresos, gastos, meses, categorías, semanas o tablas) y responde con visualizaciones dentro del chat:

- **Gráficos de Barras Comparativos (Ingresos vs Gastos):**
  - **Por Categorías**
  - **Por Semanas**
  - **Por Meses**
- **Tabla Mensual Ordenada del Año en Curso:** Muestra columnas estructuradas con `Mes`, `Ingresos`, `Gastos`, `Saldo Final del Mes` y el `Balance Acumulado Anual`.
- **Recomendaciones Personalizadas:** Cada gráfico o tabla incluye recomendaciones financieras basadas en el superávit/déficit o nivel de datos disponibles.
- **Control de Fechas y Privacidad:** Los cálculos están estrictamente aislados al `userId` autenticado. Si la consulta solicita un rango futuro o un periodo sin movimientos registrados, muestra una tarjeta de advertencia indicando que no hay datos para esas fechas.

### 11.2 Consultas Conversacionales con Gemini AI

Para preguntas abiertas de educación financiera, consejos de ahorro, estrategias de reducción de deuda o planificación personal, la interfaz envía la consulta al cliente **Gemini AI Studio** contextualizado con el idioma activo (`ES` / `EN`). Si el dispositivo se encuentra sin conectividad o sin clave configurada, el asistente proporciona orientación contextual de respaldo sin bloquear la experiencia.

---

## 12. Ajustes, Preferencias Globales y Control de Actualizaciones

### 12.1 Diálogo de Ajustes de la Aplicación

Se abre tocando el **avatar del usuario** en la Barra Superior o en la **Interfaz de Inicio** y seleccionando **`Ajustes y Preferencias`**:

1. **Tarjeta de Perfil:** Muestra nombre completo, `@usuario` y teléfono con prefijo de país.
2. **Idioma de la Aplicación (`🇪🇸 Español` / `🇺🇸 English`):** Al guardar, traduce de forma inmediata e integral todas las interfaces de Usuario Final y Administrador, incluidos menús, filtros, categorías del sistema, simulador, historial, Asistente de IA y usuarios de prueba.
3. **Moneda a Utilizar:** Despliega las **27 divisas** disponibles junto con el tipo de cambio oficial aplicado y una tarjeta de ejemplo en tiempo real (ej. `C$ 1,000.00 = ...`).
4. **Tema Visual:** Selector inmediato entre **`Modo Claro`** y **`Modo Oscuro`**.
5. **Preferencias Generales:**
   - **Notificaciones y Alertas:** Activa o desactiva avisos de facturas y presupuestos.
   - **Ocultar saldos por privacidad:** Enmascara los montos numéricos en pantalla.
   *(Nota: No existe opción de cifrado manual en preferencias; la seguridad de sesión se rige por las Directivas de Seguridad del Administrador).*
6. **Botones de Acción:** **`Cerrar Sesión`**, **`Cancelar`** y **`Guardar Ajustes`**.

### 12.2 Aviso de Actualización de Versión (Sincronizado con el Administrador)

Cuando el Administrador publica una nueva versión en el sistema:

- **Actualización Opcional:** Muestra las notas de cambio y permite elegir entre **`Actualizar Ahora`** o **`Seguir usando la app sin actualizar`**.
- **Actualización Obligatoria:** Bloquea el cierre del diálogo hasta que el usuario toque **`Actualizar Ahora (Obligatorio)`**.
- **Sincronización por usuario:** Una vez que el usuario final presiona actualizar sobre una versión obligatoria u opcional, dicha versión queda aceptada para ese usuario y **no volverá a mostrarse** en futuros inicios de sesión hasta que el Administrador registre una nueva versión (manual o automáticamente al modificar directivas de seguridad).

---

## 13. Interfaz de Administración (Exclusivo Rol Administrador)

Solo visible en la barra inferior (`Admin`) cuando el usuario autenticado posee rol `ADMIN` (por ejemplo, **Carlos Mendoza**). Está organizada en **7 pestañas**:

### 13.1 Pestaña 0: Entidades (Instituciones Financieras)

- **Búsqueda y Filtros:** Buscador por nombre/país/abreviatura y chips `Todas`, `Bancos`, `Billeteras`, `Activas`, `Deshabilitadas`.
- **Crear Nueva Institución (botón `+`):**
  - Tipo: `Banco` o `Billetera`.
  - País, Nombre completo y Abreviatura opcional.
  - Si es **Billetera**, exige seleccionar de un desplegable el **Banco Compatible** para validar futuras vinculaciones de los usuarios.
- **Deshabilitar / Reactivar / Eliminar:** Cada acción requiere obligatoriamente escribir un **motivo de cambio** no vacío (`ActionReasonDialog`), que queda registrado en Auditoría. Al deshabilitar una institución, los usuarios finales ven un aviso y no pueden crear nuevas cuentas/billeteras con ella.

### 13.2 Pestaña 1: Usuarios

- **Búsqueda Avanzada:** Permite filtrar por ámbito (`Todos los campos`, `Nombres/Apellidos`, `Nombre de Usuario`, `Correo`, `Teléfono`) y por estado (`Todos`, `Activos`, `Suspendidos`).
- **Tarjetas de Usuario:** Muestran nombre, rol, correo, teléfono, estado y un botón de **ojo** para revelar u ocultar la contraseña del usuario auditado.
- **Acciones por Usuario:**
  - **`Ver Cuentas`:** Abre un resumen de todas las cuentas bancarias y billeteras vinculadas de ese usuario.
  - **`Hacer Admin` / `Hacer Usuario`:** Cambia el rol del usuario.
  - **`Suspender` / `Reactivar`:** Exige motivo obligatorio; un usuario suspendido no puede iniciar sesión.
  - **`Eliminar`:** Exige motivo obligatorio para borrar al usuario.
  *(Protección de integridad: el sistema impide que el administrador se suspenda o elimine a sí mismo).*

### 13.3 Pestaña 2: Auditoría

- **Filtros:** Buscador por usuario, IP, detalle o evento; filtros por módulo (`Inicios Sesión`, `Seguridad`, `Proveedores`, `Usuarios`, `Versiones`) y por resultado (`Todos`, `Exitosos`, `Fallidos`).
- **Detalle de Bitácora:** Muestra fecha, hora exacta, severidad (`INFO`, `WARNING`, `CRITICAL`), dirección IP, usuario involucrado y descripción del evento. Si ningún evento coincide con los filtros, indica *"No hay registros para esas especificaciones dadas"*.

### 13.4 Pestaña 3: Monitor de APIs

Supervisa en tiempo real la salud de los **4 servicios integrados**:
1. `Gemini AI Studio API`
2. `BCN Official Exchange Rates API`
3. `Financial Institutions Gateway API`
4. `Push Notification & Bill Reminders API`

- **Indicadores Semáforo:** `Verde` (óptimo), `Amarillo` (degradado), `Rojo` (crítico), mostrando latencia (`ms`), porcentaje de disponibilidad (*uptime*), tasa de error y total de llamadas.
- **Controles:** **`Consultar APIs`** (ejecuta prueba de latencia/estado), **`Simular Error`** (prueba la detección de incidentes) y **`Restablecer`**.
- **Alerta Crítica:** Si la tasa de error en los últimos 15 minutos es igual o superior al **`15%`**, se muestra un banner rojo de alerta: **`Fallo detectado en API externa`**.

### 13.5 Pestaña 4: Versiones

- **Publicar Nueva Versión:** Formulario con **`Número de Versión`**, **`Notas de Cambio`** y selector/interruptor **`Obligatoria`** vs **`Opcional`**.
- **Historial de Versiones:** Muestra cada versión guardada cronológicamente con etiquetas `VIGENTE`, `OBLIGATORIA` u `OPCIONAL`, autor, fecha y notas de cambio.
- **Sincronización:** Toda versión marcada como obligatoria despliega el aviso de actualización en la interfaz de los usuarios finales (incluidos los de prueba) en su sesión o siguiente ingreso hasta que la acepten.

### 13.6 Pestaña 5: Seguridad (Directivas Globales)

Permite configurar en tiempo real las reglas de acceso y registro de toda la plataforma:

- **Intentos máximos de inicio de sesión** (`maxLoginAttempts`, por defecto `3`)
- **Duración de bloqueo temporal en segundos** (`lockoutDurationSeconds`, por defecto `300`)
- **Edad mínima de registro** (`minAge`, por defecto `18`)
- **Longitud máxima de nombres y apellidos** (`maxNameLength`, por defecto `70`)
- **Longitud mínima y máxima de contraseña** (`passwordMinLength` `8`, `passwordMaxLength` `20`)
- **Interruptores de complejidad:** Requerir mayúscula, requerir minúscula, requerir número, requerir carácter especial, requerir nombres capitalizados.
- **Botones:** **`Restablecer`** (vuelve a valores por defecto) y **`Guardar Directivas`**.
- **Publicación Automática:** Al guardar cambios en las directivas de seguridad, el sistema **genera y publica automáticamente una nueva versión de actualización obligatoria** en el Historial de Versiones para notificar a los usuarios finales.

### 13.7 Pestaña 6: Categorías

- **Listado:** Muestra todas las categorías del sistema y las categorías personalizadas creadas por el administrador, permitiendo eliminar las personalizadas.
- **Crear Nueva Categoría (botón `+`):** Flujo guiado en pasos claros:
  1. Seleccionar **Tipo**: `Gasto` o `Ingreso`.
  2. Seleccionar **Categoría General**:
     - Para *Gasto*: `Gastos Fijos`, `Gastos Variables` o `Pago de Deudas`.
     - Para *Ingreso*: `Ingresos Activos` o `Ingresos Pasivos`.
  3. Ingresar el **Nombre** de la nueva categoría.
  4. Elegir un ícono en el **Selector de +500 Íconos**, organizado en **10 grupos temáticos** (`Finanzas`, `Comida`, `Hogar`, `Viajes`, `Salud`, `Tecnología`, `Educación`, `Naturaleza`, `Estilo` y `Cripto`).
- **Sincronización Inmediata:** Al guardar, la categoría aparece automáticamente en los formularios de movimientos, filtros y presupuestos de todos los usuarios finales (incluido el usuario de prueba).

---

## 14. Resumen de Reglas de Validación, Límites y Solución de Problemas

### 14.1 Tabla Rápida de Límites Operativos

| Operación | Límite / Regla Aplicada |
| :--- | :--- |
| Cuenta Débito — **Ahorro** | Saldo inicial mínimo: `C$ 500.00` |
| Cuenta Débito — **Corriente** | Saldo inicial mínimo: `C$ 2,500.00` |
| Cuenta Débito — **Nómina** | Saldo inicial mínimo: `C$ 0.00` |
| Cuenta de **Crédito** | Límite de crédito mínimo: `C$ 7,000.00` |
| Número de cuenta bancaria | Mínimo `6` dígitos, único entre bancos |
| Billetera Digital | Mismo número telefónico del usuario + vinculada a cuenta activa del mismo banco |
| Transferencia con Billetera Digital | Máximo `C$ 15,000.00` |
| Transferencia Banco a Banco | Máximo `C$ 200,000.00` |
| Comisión por transferencia | `C$ 0.00` mismo banco / `C$ 80.00` bancos diferentes |
| Movimiento `< $10,000 USD` | Registro directo |
| Movimiento `$10,000` – `$1,000,000 USD` | Aviso de confirmación por monto inusualmente alto |
| Movimiento `> $1,000,000 USD` | Bloqueo automático; requiere escribir **`ACEPTAR`** |
| Factura / Recordatorio de pago | Fecha estrictamente posterior a hoy; alerta urgente si vence en `<= 48 horas` |

### 14.2 Solución de Problemas Frecuentes

| Situación | Causa Probable y Qué Hacer |
| :--- | :--- |
| **Aparece un contador regresivo al intentar iniciar sesión** | Se superó el límite de intentos fallidos (`3` por defecto). Espere a que el contador `MM:SS` llegue a `00:00` e intente con la contraseña correcta o use las cuentas de prueba **Elena** / **Carlos**. |
| **No puedo vincular una Billetera Digital** | Verifique dos requisitos: (1) debe tener previamente una **cuenta bancaria activa de la misma marca** (ej. cuenta BAC para Kash BAC) y (2) el teléfono debe ser el mismo registrado en su perfil de usuario. |
| **El sistema bloquea un ingreso o gasto muy grande** | Su monto convertido a dólares supera `$1,000,000 USD`. Verifique que no haya dígitos de más; si el monto es real, escriba **`ACEPTAR`** en el diálogo de confirmación. |
| **Un banco o billetera aparece deshabilitado al crear cuenta** | El Administrador deshabilitó esa institución en la pestaña **Entidades**. Revise el banner superior en Billeteras o reactive la institución desde el perfil de Administrador. |
| **La app solicita actualizar de forma obligatoria al abrir** | El Administrador publicó una nueva versión obligatoria o modificó las Directivas de Seguridad. Toque **`Actualizar Ahora (Obligatorio)`**; una vez aceptada, no volverá a aparecer hasta la siguiente versión publicada. |
| **El Asistente de IA indica que no hay datos para esas fechas** | Se consultó un periodo futuro o un intervalo sin movimientos registrados en su cuenta. Pruebe seleccionando el año actual o consulte luego de registrar movimientos en ese periodo. |
