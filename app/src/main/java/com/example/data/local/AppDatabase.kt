package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        BillReminderEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzas_inteligentes.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val userDao = db.userDao()
            val transactionDao = db.transactionDao()
            val budgetDao = db.budgetDao()
            val goalDao = db.savingsGoalDao()
            val billDao = db.billReminderDao()

            // Default Categories
            val defaultCategories = listOf(
                CategoryEntity(name = "Alimentación", type = "EXPENSE", iconName = "restaurant", colorHex = "#EF4444"),
                CategoryEntity(name = "Transporte", type = "EXPENSE", iconName = "directions_bus", colorHex = "#F97316"),
                CategoryEntity(name = "Salud", type = "EXPENSE", iconName = "medical_services", colorHex = "#10B981"),
                CategoryEntity(name = "Educación", type = "EXPENSE", iconName = "school", colorHex = "#3B82F6"),
                CategoryEntity(name = "Entretenimiento", type = "EXPENSE", iconName = "sports_esports", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Servicios Básicos", type = "EXPENSE", iconName = "water_drop", colorHex = "#06B6D4"),
                CategoryEntity(name = "Vivienda", type = "EXPENSE", iconName = "home", colorHex = "#6366F1"),
                CategoryEntity(name = "Ropa y Calzado", type = "EXPENSE", iconName = "checkroom", colorHex = "#EC4899"),
                CategoryEntity(name = "Otros Gastos", type = "EXPENSE", iconName = "receipt_long", colorHex = "#6B7280"),
                CategoryEntity(name = "Salario", type = "INCOME", iconName = "payments", colorHex = "#22C55E"),
                CategoryEntity(name = "Negocio Propio", type = "INCOME", iconName = "store", colorHex = "#14B8A6"),
                CategoryEntity(name = "Inversiones", type = "INCOME", iconName = "trending_up", colorHex = "#84CC16"),
                CategoryEntity(name = "Otros Ingresos", type = "INCOME", iconName = "savings", colorHex = "#A855F7")
            )
            categoryDao.insertCategories(defaultCategories)

            // Pre-populated Admin User
            val adminId = userDao.insertUser(
                UserEntity(
                    firstName = "Carlos",
                    lastName = "Mendoza",
                    age = 32,
                    phone = "88997766",
                    email = "admin@finanzas.com",
                    passwordHash = "Admin123!",
                    role = "ADMIN"
                )
            )

            // Pre-populated Regular User
            val demoUserId = userDao.insertUser(
                UserEntity(
                    firstName = "Elena",
                    lastName = "Gómez",
                    age = 26,
                    phone = "77889900",
                    email = "elena@finanzas.com",
                    passwordHash = "Elena2026!",
                    role = "USER"
                )
            )

            val now = System.currentTimeMillis()
            val day = 86400000L

            // Seed transactions for demo user
            val demoTransactions = listOf(
                TransactionEntity(userId = demoUserId, title = "Pago de Quincena", amount = 18500.0, type = "INCOME", category = "Salario", dateMillis = now - (day * 3), note = "Nómina mensual"),
                TransactionEntity(userId = demoUserId, title = "Supermercado La Unión", amount = 2450.0, type = "EXPENSE", category = "Alimentación", dateMillis = now - (day * 2), note = "Compras para la quincena"),
                TransactionEntity(userId = demoUserId, title = "Gasolina y Transporte", amount = 850.0, type = "EXPENSE", category = "Transporte", dateMillis = now - (day * 1), note = "Recarga de combustible"),
                TransactionEntity(userId = demoUserId, title = "Farmacia y Medicinas", amount = 480.0, type = "EXPENSE", category = "Salud", dateMillis = now - (day * 4), note = "Vitaminas y analgésicos"),
                TransactionEntity(userId = demoUserId, title = "Cine y Cena Rápida", amount = 650.0, type = "EXPENSE", category = "Entretenimiento", dateMillis = now - (day * 5), note = "Salida fin de semana"),
                TransactionEntity(userId = demoUserId, title = "Venta de Proyecto Freelance", amount = 6200.0, type = "INCOME", category = "Negocio Propio", dateMillis = now - (day * 8), note = "Diseño gráfico cliente"),
                TransactionEntity(userId = demoUserId, title = "Recibo de Luz Enel", amount = 1100.0, type = "EXPENSE", category = "Servicios Básicos", dateMillis = now - (day * 6), note = "Servicio eléctrico"),
                TransactionEntity(userId = demoUserId, title = "Curso de Finanzas Online", amount = 750.0, type = "EXPENSE", category = "Educación", dateMillis = now - (day * 10), note = "Capacitación")
            )
            demoTransactions.forEach { transactionDao.insertTransaction(it) }

            // Seed Monthly Budgets
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Alimentación", monthlyLimit = 5000.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Transporte", monthlyLimit = 2000.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Entretenimiento", monthlyLimit = 1500.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Servicios Básicos", monthlyLimit = 2200.0, monthYear = "2026-09"))

            // Seed Savings Goals
            goalDao.insertGoal(SavingsGoalEntity(userId = demoUserId, title = "Fondo de Emergencia", targetAmount = 30000.0, currentAmount = 14500.0, targetDateMillis = now + (day * 120), iconName = "shield"))
            goalDao.insertGoal(SavingsGoalEntity(userId = demoUserId, title = "Viaje de Vacaciones", targetAmount = 15000.0, currentAmount = 8200.0, targetDateMillis = now + (day * 90), iconName = "flight"))

            // Seed Bill Reminders
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Recibo de Agua Potable", amount = 380.0, dueDateMillis = now + (day * 3), category = "Servicios Básicos", isPaid = false))
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Internet de Fibra Óptica", amount = 950.0, dueDateMillis = now + (day * 7), category = "Servicios Básicos", isPaid = false))
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Cuota Préstamo Personal", amount = 2500.0, dueDateMillis = now + (day * 15), category = "Otros Gastos", isPaid = false))
        }
    }
}
