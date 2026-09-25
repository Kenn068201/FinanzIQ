# 📱 FinanzIQ — Gestión Financiera Inteligente y Personal

---

## 📌 1. Nombre del Proyecto
**FinanzIQ**

---

## 📖 2. Descripción Breve del Proyecto
**FinanzIQ** es una aplicación móvil moderna e intuitiva diseñada para ayudarte a tomar el control total de tu dinero día a día. Te permite registrar tus ingresos y gastos, gestionar múltiples billeteras y cuentas bancarias, crear presupuestos mensuales por categorías, planificar metas de ahorro con recordatorios de pago de servicios, y recibir recomendaciones personalizadas mediante un asistente inteligente.

---

## 🎯 3. Objetivo General
Proporcionar a las personas una herramienta digital accesible, segura y fácil de usar que simplifique la administración de sus finanzas personales, facilitando el ahorro y la toma de decisiones informadas para mejorar su bienestar económico.

---

## 🎯 4. Objetivos Específicos
* **Facilitar el registro diario:** Registrar rápidamente ingresos y gastos vinculados a cuentas o billeteras reales con validación automática de saldos disponibles.
* **Fomentar el hábito del ahorro:** Crear y dar seguimiento visual al progreso de metas de ahorro personales a corto, mediano y largo plazo.
* **Controlar el gasto mediante presupuestos:** Establecer límites mensuales por categorías de gasto y recibir alertas visuales antes de sobrepasar el presupuesto.
* **Monitorear cuentas y servicios:** Administrar recordatorios de pago de facturas y servicios fijos para evitar recargos o moras.
* **Visualizar estadísticas claras:** Mostrar gráficos interactivos de barras y proporciones para comprender en qué se gasta el dinero.
* **Brindar orientación financiera:** Ofrecer un asistente inteligente con inteligencia artificial para responder dudas y dar recomendaciones financieras adaptadas a tus hábitos.

---

## 🛠️ 5. Stack Tecnológico Utilizado
* **Lenguaje de programación:** [Kotlin](https://kotlinlang.org/) — Lenguaje moderno, seguro y recomendado para desarrollo Android.
* **Interfaz de usuario (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material Design 3 — Diseño fluido, moderno y responsivo.
* **Arquitectura:** MVVM (Model-View-ViewModel) con StateFlow y Coroutines para un manejo de estado reactivo y fluido.
* **Base de datos local:** [Room Database (SQLite)](https://developer.android.com/training/data-storage/room) — Almacenamiento seguro, rápido y 100% offline en tu propio dispositivo.
* **Seguridad y Cifrado:** [AndroidX Security Crypto (EncryptedSharedPreferences)](https://developer.android.com/topic/security/data) — Protección de sesiones y datos sensibles.
* **Inteligencia Artificial:** Google Gemini API — Asesoría financiera inteligente y personalizada.

---

## 💡 6. Problema que Resuelve
Muchas personas experimentan dificultades para saber a dónde se va su dinero a fin de mes, acumulan deudas sin darse cuenta o no logran cumplir sus metas de ahorro debido a:
1. **Falta de registro claro:** Uso de libretas o notas desordenadas donde se olvidan los gastos pequeños ("gastos hormiga").
2. **Descontrol entre múltiples cuentas:** Dificultad para saber cuánto dinero real hay repartido entre efectivo, cuentas bancarias y billeteras digitales.
3. **Falta de alertas en gastos fijos:** Olvido de fechas de vencimiento de recibos y facturas.
4. **Ausencia de presupuestos claros:** Gastar más de lo ingresado por no tener límites fijados por categoría.

**FinanzIQ** soluciona esto centralizando toda tu vida financiera en la palma de tu mano con validaciones automáticas, reportes visuales y recordatorios a tiempo.

---

## 👥 7. Roles del Sistema y sus Funciones

### 1. 👤 Usuario Final (Personal)
* **Gestión de Billeteras y Cuentas:** Crear cuentas en efectivo, débito, crédito o ahorros y consultar saldos actualizados.
* **Registro de Transacciones:** Registrar ingresos y gastos seleccionando la billetera de origen (con validación de saldo disponible).
* **Presupuestos y Categorías:** Asignar presupuestos mensuales y revisar el porcentaje consumido.
* **Metas de Ahorro:** Definir objetivos económicos y registrar abonos progresivos.
* **Facturas y Recibos:** Agendar pagos recurrentes y marcarlos como pagados.
* **Análisis Gráfico:** Consultar reportes mensuales y semanales con desglose por categorías y subcategorías.
* **Asistente IA:** Consultar consejos y tips de educación financiera en tiempo real.

### 2. 🛡️ Administrador del Sistema
* **Gestión de Categorías Globales:** Crear, modificar y organizar las categorías y subcategorías disponibles en el sistema.
* **Administración de Usuarios y Roles:** Supervisar las cuentas registradas y gestionar permisos de acceso.
* **Configuración del Catálogo:** Mantener la consistencia de tipos de cuenta y grupos de gastos predeterminados.

---

## 📂 8. Estructura del Proyecto (Carpetas y Archivos)

```text
app/src/main/java/com/example/
├── MainActivity.kt                 # Punto de entrada principal de la aplicación
├── data/                           # Capa de datos y persistencia
│   ├── local/
│   │   ├── AppDatabase.kt          # Configuración y migraciones de Room Database
│   │   ├── Daos.kt                 # Interfaces de acceso a datos (Transacciones, Cuentas, Metas, etc.)
│   │   └── Entities.kt             # Modelos de tablas (Usuario, Billetera, Transacción, Presupuesto, etc.)
├── ui/                             # Capa de interfaz de usuario (Jetpack Compose)
│   ├── FinanceViewModel.kt         # ViewModel central con la lógica de negocio y estados reactivos
│   ├── admin/
│   │   └── AdminScreen.kt          # Interfaz de gestión para el Administrador (Usuarios y Categorías)
│   ├── ai_assistant/
│   │   └── AiAssistantScreen.kt    # Interfaz del Asistente Financiero con Inteligencia Artificial
│   ├── analytics/
│   │   └── AnalyticsScreen.kt      # Interfaz de reportes, métricas y gráficos de barras
│   ├── auth/
│   │   └── AuthScreen.kt           # Interfaces de Registro e Inicio de Sesión
│   ├── bills/
│   │   └── BillsScreen.kt          # Interfaz de control y recordatorios de facturas por pagar
│   ├── budgets/
│   │   └── BudgetsScreen.kt        # Interfaz de presupuestos por categoría y límites mensuales
│   ├── components/                 # Componentes y widgets reutilizables (Tarjetas, Diálogos, Formularios)
│   ├── dashboard/
│   │   └── DashboardScreen.kt      # Interfaz principal / Resumen general del usuario
│   ├── navigation/
│   │   └── MainAppNavigation.kt    # Enrutamiento y barra de navegación inferior adaptativa
│   ├── savings/
│   │   └── SavingsGoalsScreen.kt   # Interfaz de metas de ahorro y alcancías virtuales
│   ├── theme/
│   │   ├── Color.kt                # Paleta de colores oficial (Material 3)
│   │   ├── Theme.kt                # Configuración del tema claro y oscuro
│   │   └── Type.kt                 # Tipografías del sistema
│   ├── transactions/
│   │   └── TransactionsScreen.kt   # Interfaz y formulario de registro de ingresos y gastos
│   └── wallets/
│       └── WalletsScreen.kt        # Interfaz de administración de billeteras y cuentas
└── util/
    ├── EncryptedPrefs.kt           # Almacenamiento seguro cifrado para sesiones
    ├── Localization.kt             # Soporte de idiomas y textos localizados
    └── UserPreferences.kt          # Preferencias generales del usuario (tema, moneda, etc.)
```

---

## 🚀 9. Proceso de Ejecución

Para abrir y ejecutar este proyecto en tu computadora:

### Requisitos Previos
* **Android Studio** (versión Ladybug o superior recomendada).
* **JDK 17** o superior instalado.
* Dispositivo físico Android (con depuración USB activada) o un **Emulador Android** (API 26+).

### Pasos para Ejecutar
1. **Clonar o descargar el proyecto:**
   Descarga el código fuente en tu computadora y descomprímelo si está en formato ZIP.
2. **Abrir en Android Studio:**
   Abre Android Studio y selecciona **Open**, luego navega hasta la carpeta del proyecto y selecciónala.
3. **Sincronizar dependencias:**
   Android Studio descargará automáticamente las dependencias de Gradle necesarias (`Sync Project with Gradle Files`).
4. **Ejecutar la aplicación:**
   * Selecciona tu emulador o dispositivo en la barra superior.
   * Haz clic en el botón verde de **Run** (o presiona `Shift + F10`).
   * ¡Listo! La app se compilará y se abrirá en la pantalla de bienvenida de FinanzIQ.
