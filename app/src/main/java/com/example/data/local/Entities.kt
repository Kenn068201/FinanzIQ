package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val phone: String,
    val phonePrefix: String = "+505",
    val email: String,
    val passwordHash: String,
    val role: String = "USER", // "USER" or "ADMIN"
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
