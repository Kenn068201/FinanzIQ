package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val username: String = "", // Nombre de usuario único para inicio de sesión y gestión en panel de admin
    val age: Int,
    val phone: String,
    val phonePrefix: String = "+505",
    val email: String,
    val passwordHash: String,
    val role: String = "USER", // "USER" o "ADMIN"
    val isBlocked: Boolean = false, // Indica si el usuario ha sido suspendido o bloqueado por el administrador
    val statusReason: String = "", // Motivo obligatorio registrado por el admin al bloquear o reactivar
    val preferredCurrency: String = "C$ NIO",
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val subCategory: String = "",
    val categoryGroup: String = "", // "GASTOS_FIJOS", "GASTOS_VARIABLES", "PAGO_DEUDAS", "INGRESO_ACTIVO", "INGRESO_PASIVO"
    val incomeSubType: String = "", // "ACTIVE" or "PASSIVE"
    val frequency: String = "", // "MENSUAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL", "ESPORADICO"
    val payoutDate: String = "",
    val destination: String = "", // wallet, bank, broker
    val dateMillis: Long,
    val note: String = "",
    val isRecurring: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String // e.g. "2026-09"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDateMillis: Long,
    val iconName: String = "savings"
)

@Entity(tableName = "bill_reminders")
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val amount: Double,
    val dueDateMillis: Long,
    val category: String,
    val isPaid: Boolean = false,
    val recurringType: String = "MONTHLY" // "ONCE", "MONTHLY", "WEEKLY"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "EXPENSE" or "INCOME"
    val iconName: String,
    val colorHex: String,
    val isCustom: Boolean = false
)

/**
 * Entidad de Herramientas Financieras (Cuentas Bancarias y Billeteras Digitales).
 * Permite gestionar cuentas de débito (ahorro, corriente, nómina), crédito con su límite,
 * y billeteras vinculadas al teléfono del usuario y cuenta bancaria de respaldo.
 * Incluye bandera de estado activo/deshabilitado para conservar el historial intacto.
 */
@Entity(tableName = "financial_accounts")
data class FinancialAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val toolType: String, // "CUENTA" o "BILLETERA"
    val bankName: String, // "BAC", "LAFISE", "Ficohsa", "Banpro", "Avanz", "BDF", etc.
    val accountType: String, // "DEBITO", "CREDITO", "BILLETERA"
    val debitSubType: String = "", // "AHORRO", "CORRIENTE", "NOMINA"
    val accountNumber: String, // Número de cuenta o identificador
    val creditLimit: Double = 0.0, // Límite para cuentas de crédito
    val balance: Double = 0.0, // Saldo actual disponible
    val associatedPhone: String = "", // Número telefónico (para billeteras digitales)
    val linkedBank: String = "", // Banco de la cuenta vinculada a la billetera
    val linkedAccountType: String = "", // Tipo de cuenta vinculada ("DEBITO" / "CREDITO")
    val linkedDebitSubType: String = "", // Subtipo de cuenta vinculada ("AHORRO" / "CORRIENTE" / "NOMINA")
    val linkedAccountNumber: String = "", // Número de cuenta bancaria vinculada
    val isActive: Boolean = true, // true: Activa, false: Deshabilitada
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entidad que representa el catálogo de Instituciones Financieras (Bancos y Billeteras Digitales).
 * Permite al Administrador gestionar las entidades disponibles en la aplicación, desactivarlas con un motivo
 * obligatorio o crear nuevas entidades especificando país de origen, nombre completo, abreviatura y banco compatible.
 */
@Entity(tableName = "financial_institutions")
data class FinancialInstitutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val country: String, // País de origen de la entidad (ej. Nicaragua, Estados Unidos, etc.)
    val fullName: String, // Nombre completo de la entidad financiera
    val abbreviation: String = "", // Abreviatura opcional (ej. BAC, BANPRO, BDF)
    val type: String, // Tipo de entidad: "BANCO" o "BILLETERA"
    val supportedBankBrands: String = "", // Si es billetera: nombre o marca del banco compatible al cual se puede vincular
    val isActive: Boolean = true, // true: Disponible para vincular, false: Desactivada por admin
    val statusReason: String = "", // Motivo de la desactivación o activación ingresado por el administrador
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entidad de Directivas de Seguridad Globales de la Aplicación.
 * Permite al administrador ajustar parámetros de autenticación, bloqueo temporal por intentos fallidos,
 * y reglas de validación de contraseñas y registro de usuarios para toda la plataforma.
 */
@Entity(tableName = "security_policies")
data class SecurityPolicyEntity(
    @PrimaryKey
    val id: Long = 1,
    val maxLoginAttempts: Int = 3, // Intentos fallidos antes del bloqueo temporal
    val lockoutDurationSeconds: Int = 300, // Duración del bloqueo en segundos (300s = 5 minutos)
    val passwordMinLength: Int = 8, // Longitud mínima de contraseña
    val passwordMaxLength: Int = 20, // Longitud máxima de contraseña
    val requireUppercase: Boolean = true, // Requerir al menos una letra mayúscula
    val requireLowercase: Boolean = false, // Requerir al menos una letra minúscula
    val requireNumber: Boolean = true, // Requerir al menos un número
    val requireSpecialChar: Boolean = true, // Requerir al menos un carácter especial
    val minAge: Int = 18, // Edad mínima de registro
    val maxNameLength: Int = 70, // Longitud máxima permitida para nombres y apellidos
    val requireCapitalizedNames: Boolean = true, // Exigir primera letra mayúscula en nombres y apellidos
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entidad de Registro de Auditoría (Bitácora de eventos del sistema).
 * 
 * Qué hace:
 * Registra inicios de sesión exitosos y fallidos, cambios críticos en directivas de seguridad,
 * modificaciones en catálogo de proveedores/bancos, cambios de estado en cuentas de usuario
 * y publicaciones de versiones, junto a la fecha, hora e IP exacta del evento.
 * 
 * Por qué se implementa de esta forma:
 * Permite al administrador auditar la seguridad, identificar actividad sospechosa por patrones
 * y diagnosticar fallos operacionales.
 * 
 * Cómo se relaciona con el resto:
 * Se almacena en Room mediante AuditLogDao y se expone con Flow para filtrado dinámico en tiempo real.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String, // "LOGIN_SUCCESS", "LOGIN_FAILED", "SECURITY_CHANGE", "INSTITUTION_CHANGE", "USER_STATUS_CHANGE", "VERSION_UPDATE"
    val severity: String = "INFO", // "INFO", "WARNING", "CRITICAL"
    val userId: Long? = null,
    val username: String = "Sistema",
    val description: String,
    val ipAddress: String, // IP exacta donde se originó el evento (ej. "192.168.1.105")
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
) {
    val isSuccess: Boolean
        get() = severity != "ERROR" && !eventType.contains("FAIL")

    val action: String
        get() = description

    val eventDate: String
        get() {
            val date = java.util.Date(timestamp)
            return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date)
        }

    val eventTime: String
        get() {
            val date = java.util.Date(timestamp)
            return java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(date)
        }
}

/**
 * Entidad para el Control de Versiones de la Aplicación.
 * 
 * Qué hace:
 * Guarda los números de versión publicados, sus notas de cambio y si la actualización es
 * de carácter obligatorio o de carácter opcional para los usuarios finales.
 * 
 * Por qué se implementa de esta forma:
 * Cumple con el requerimiento de sincronizar la versión del sistema con la notificación
 * y bloqueo de uso para versiones obsoletas si el administrador la marca como obligatoria.
 * 
 * Cómo se relaciona con el resto:
 * El Scaffold y la navegación comparan la versión del cliente contra la última versión registrada.
 */
@Entity(tableName = "app_versions")
data class AppVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val versionNumber: String, // Número de versión (ej. "1.2.0")
    val releaseNotes: String, // Notas de cambios y novedades (no nulo)
    val isMandatory: Boolean, // true: Obligatoria (bloqueante), false: Opcional
    val publishedBy: String = "Admin",
    val releaseDate: Long = System.currentTimeMillis()
)

/**
 * Entidad de Métricas para el Monitor de APIs Integradas.
 * 
 * Qué hace:
 * Registra el uso, latencia en ms, código de respuesta y resultado de las APIs consumidas
 * por la aplicación (Gemini AI, Tasas de Cambio BCN, Gateway Financiero y Notificaciones).
 * 
 * Por qué se implementa de esta forma:
 * Permite calcular tasas de error porcentuales, latencia media, uptime y emitir alertas visuales
 * si el margen de error supera el 15% en los últimos 15 minutos.
 */
@Entity(tableName = "api_metrics")
data class ApiMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val apiName: String, // Identificador de la API integrada
    val endpoint: String,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val statusCode: Int = 200,
    val errorMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


