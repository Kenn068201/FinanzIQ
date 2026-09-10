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
