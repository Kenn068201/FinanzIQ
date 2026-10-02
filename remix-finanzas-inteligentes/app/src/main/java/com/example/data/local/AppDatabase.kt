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
        CategoryEntity::class,
        FinancialAccountEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun financialAccountDao(): FinancialAccountDao

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
                    .fallbackToDestructiveMigration()
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

        /**
         * Población inicial de la base de datos con usuarios de prueba y cuentas reales de Nicaragua.
         * Genera las cuentas bancarias (BAC, LAFISE, Banpro, Ficohsa, Avanz, BDF) y billeteras asociadas.
         */
        suspend fun populateInitialData(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val userDao = db.userDao()
            val transactionDao = db.transactionDao()
            val budgetDao = db.budgetDao()
            val goalDao = db.savingsGoalDao()
            val billDao = db.billReminderDao()
            val accountDao = db.financialAccountDao()

            // Categorías por defecto del sistema
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

            // Usuario de prueba: Administrador Carlos
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

            // Usuario de prueba: Cliente Elena
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

            // Cuentas y billeteras con formato de Nicaragua para Elena Gómez (Tel: 77889900)
            val elenaAccounts = listOf(
                FinancialAccountEntity(
                    userId = demoUserId,
                    toolType = "CUENTA",
                    bankName = "BAC",
                    accountType = "DEBITO",
                    debitSubType = "AHORRO",
                    accountNumber = "001-987654-3",
                    balance = 14200.0,
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = demoUserId,
                    toolType = "CUENTA",
                    bankName = "LAFISE",
                    accountType = "DEBITO",
                    debitSubType = "NOMINA",
                    accountNumber = "204-554433-1",
                    balance = 9800.0,
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = demoUserId,
                    toolType = "CUENTA",
                    bankName = "Banpro",
                    accountType = "CREDITO",
                    debitSubType = "",
                    accountNumber = "402-112233-9",
                    creditLimit = 35000.0,
                    balance = 18500.0,
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = demoUserId,
                    toolType = "BILLETERA",
                    bankName = "Banpro",
                    accountType = "BILLETERA",
                    debitSubType = "",
                    accountNumber = "77889900",
                    balance = 3450.0,
                    associatedPhone = "77889900",
                    linkedBank = "Banpro",
                    linkedAccountType = "DEBITO",
                    linkedDebitSubType = "AHORRO",
                    linkedAccountNumber = "402-998877-0",
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = demoUserId,
                    toolType = "BILLETERA",
                    bankName = "BAC",
                    accountType = "BILLETERA",
                    debitSubType = "",
                    accountNumber = "77889900",
                    balance = 1200.0,
                    associatedPhone = "77889900",
                    linkedBank = "BAC",
                    linkedAccountType = "DEBITO",
                    linkedDebitSubType = "AHORRO",
                    linkedAccountNumber = "001-987654-3",
                    isActive = true
                )
            )
            elenaAccounts.forEach { accountDao.insertAccount(it) }

            // Cuentas y billeteras con formato de Nicaragua para Carlos Mendoza (Tel: 88997766)
            val adminAccounts = listOf(
                FinancialAccountEntity(
                    userId = adminId,
                    toolType = "CUENTA",
                    bankName = "Ficohsa",
                    accountType = "DEBITO",
                    debitSubType = "CORRIENTE",
                    accountNumber = "501-887766-2",
                    balance = 45000.0,
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = adminId,
                    toolType = "CUENTA",
                    bankName = "Avanz",
                    accountType = "DEBITO",
                    debitSubType = "AHORRO",
                    accountNumber = "601-332211-8",
                    balance = 22300.0,
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = adminId,
                    toolType = "CUENTA",
                    bankName = "BDF",
                    accountType = "CREDITO",
                    debitSubType = "",
                    accountNumber = "701-445566-0",
                    creditLimit = 50000.0,
                    balance = 12000.0,
                    isActive = true
                ),
                FinancialAccountEntity(
                    userId = adminId,
                    toolType = "BILLETERA",
                    bankName = "Ficohsa",
                    accountType = "BILLETERA",
                    debitSubType = "",
                    accountNumber = "88997766",
                    balance = 5000.0,
                    associatedPhone = "88997766",
                    linkedBank = "Ficohsa",
                    linkedAccountType = "DEBITO",
                    linkedDebitSubType = "CORRIENTE",
                    linkedAccountNumber = "501-887766-2",
                    isActive = true
                )
            )
            adminAccounts.forEach { accountDao.insertAccount(it) }

            val now = System.currentTimeMillis()
            val day = 86400000L

            // Movimientos de demostración para Elena
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

            // Presupuestos mensuales
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Alimentación", monthlyLimit = 5000.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Transporte", monthlyLimit = 2000.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Entretenimiento", monthlyLimit = 1500.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Servicios Básicos", monthlyLimit = 2200.0, monthYear = "2026-09"))

            // Metas de ahorro
            goalDao.insertGoal(SavingsGoalEntity(userId = demoUserId, title = "Fondo de Emergencia", targetAmount = 30000.0, currentAmount = 14500.0, targetDateMillis = now + (day * 120), iconName = "shield"))
            goalDao.insertGoal(SavingsGoalEntity(userId = demoUserId, title = "Viaje de Vacaciones", targetAmount = 15000.0, currentAmount = 8200.0, targetDateMillis = now + (day * 90), iconName = "flight"))

            // Recordatorios de facturas
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Recibo de Agua Potable", amount = 380.0, dueDateMillis = now + (day * 3), category = "Servicios Básicos", isPaid = false))
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Internet de Fibra Óptica", amount = 950.0, dueDateMillis = now + (day * 7), category = "Servicios Básicos", isPaid = false))
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Cuota Préstamo Personal", amount = 2500.0, dueDateMillis = now + (day * 15), category = "Otros Gastos", isPaid = false))
        }
    }
}
