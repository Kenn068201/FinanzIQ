package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        BillReminderEntity::class,
        CategoryEntity::class,
        FinancialAccountEntity::class,
        FinancialInstitutionEntity::class,
        SecurityPolicyEntity::class,
        AuditLogEntity::class,
        AppVersionEntity::class,
        ApiMetricEntity::class
    ],
    version = 6,
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
    abstract fun financialInstitutionDao(): FinancialInstitutionDao
    abstract fun securityPolicyDao(): SecurityPolicyDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun appVersionDao(): AppVersionDao
    abstract fun apiMetricDao(): ApiMetricDao

    /**
     * Garantiza que los datos iniciales ya terminaron de cargarse antes de usar la base de datos.
     * Qué hace: espera con seguridad a que la carga inicial termine si aún está en curso.
     * Por qué se implementa así: evita que al abrir la app por primera vez, el login o las
     * versiones intenten leer tablas vacías o choquen creando registros duplicados.
     * Cómo se relaciona: FinanceViewModel lo llama antes de iniciar sesión o observar versiones.
     */
    suspend fun ensureSeeded() {
        ensureSeededInternal(this)
    }

    companion object {
        private const val DB_NAME = "finanzas_inteligentes.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val dbLock = Any()
        private val seedMutex = Mutex()
        private val appDbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(DatabaseCallback())
                .build()
        }

        fun getDatabase(context: Context, @Suppress("UNUSED_PARAMETER") scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(dbLock) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Verifica los datos iniciales y repara la base si detecta un esquema incompatible o archivo dañado.
         * Qué hace: intenta cargar o completar los datos base; si Room falla por tablas viejas de una
         * actualización previa, borra el archivo dañado, crea una base limpia y carga a Elena y Carlos.
         * Por qué se implementa así: evita que instalaciones anteriores bloqueen el registro y el acceso.
         * Cómo se relaciona: FinanceViewModel lo usa al arrancar, iniciar sesión y registrar usuarios.
         */
        suspend fun ensureSeededOrRecover(
            context: Context,
            @Suppress("UNUSED_PARAMETER") scope: CoroutineScope = appDbScope
        ): Pair<AppDatabase, Boolean> {
            val appContext = context.applicationContext
            val currentDb = getDatabase(appContext, appDbScope)
            return try {
                ensureSeededInternal(currentDb)
                currentDb to false
            } catch (e: Exception) {
                if (isRecoverableDatabaseError(e)) {
                    Log.w("AppDatabase", "Esquema o archivo incompatible detectado; recreando base de datos", e)
                    val freshDb = resetAndRebuildDatabase(appContext)
                    ensureSeededInternal(freshDb)
                    freshDb to true
                } else {
                    throw e
                }
            }
        }

        private fun isRecoverableDatabaseError(error: Throwable): Boolean {
            var current: Throwable? = error
            while (current != null) {
                if (current is IllegalStateException || current is android.database.sqlite.SQLiteException) {
                    return true
                }
                current = current.cause
            }
            return false
        }

        private suspend fun resetAndRebuildDatabase(appContext: Context): AppDatabase =
            withContext(Dispatchers.IO) {
                synchronized(dbLock) {
                    runCatching { INSTANCE?.close() }
                    INSTANCE = null
                    appContext.deleteDatabase(DB_NAME)
                    val dbFile = appContext.getDatabasePath(DB_NAME)
                    listOf("", "-wal", "-shm", "-journal").forEach { suffix ->
                        val target = java.io.File(dbFile.absolutePath + suffix)
                        if (target.exists()) target.delete()
                    }
                    buildDatabase(appContext).also { INSTANCE = it }
                }
            }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    appDbScope.launch {
                        runCatching { ensureSeededInternal(database) }
                            .onFailure { Log.e("AppDatabase", "Error al inicializar datos base", it) }
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    appDbScope.launch {
                        runCatching { ensureSeededInternal(database) }
                            .onFailure { Log.e("AppDatabase", "Error al repoblar tras migración", it) }
                    }
                }
            }
        }

        /**
         * Verifica y repara de forma segura los datos iniciales y usuarios de prueba.
         * Qué hace: si la base está vacía carga todo; si ya existía con datos parciales
         * o sin Elena/Carlos, agrega únicamente los usuarios de prueba y catálogos faltantes.
         * Por qué se implementa así: evita que copias de seguridad o instalaciones previas
         * bloqueen el acceso con los usuarios de prueba al exportar la app.
         * Cómo se relaciona: FinanceViewModel la llama antes de iniciar sesión o cargar listas.
         */
        private suspend fun ensureSeededInternal(db: AppDatabase) {
            withContext(Dispatchers.IO) {
                seedMutex.withLock {
                    if (db.userDao().getUserCount() == 0) {
                        db.withTransaction {
                            if (db.userDao().getUserCount() == 0) {
                                populateInitialData(db)
                            }
                        }
                    } else {
                        db.withTransaction {
                            ensureDemoUsersAndDefaults(db)
                        }
                    }
                }
            }
        }

        private suspend fun ensureDemoUsersAndDefaults(db: AppDatabase) {
            val policyDao = db.securityPolicyDao()
            if (policyDao.getSecurityPolicyDirect() == null) {
                policyDao.insertOrUpdatePolicy(defaultSecurityPolicy())
            }
            if (db.financialInstitutionDao().getInstitutionCount() == 0) {
                db.financialInstitutionDao().insertInstitutions(defaultInstitutions())
            }
            if (db.categoryDao().getCategoryCount() == 0) {
                db.categoryDao().insertCategories(defaultCategories())
            }
            val userDao = db.userDao()
            val accountDao = db.financialAccountDao()

            // Restaura a Carlos (Admin) si falta o si quedó marcado erróneamente como bloqueado
            val existingAdmin = userDao.getUserByIdentifier("admin@finanzas.com")
                ?: userDao.getUserByIdentifier("admin")
            val adminId = if (existingAdmin == null) {
                userDao.insertUser(defaultAdminUser())
            } else {
                if (existingAdmin.isBlocked || existingAdmin.passwordHash.isBlank()) {
                    userDao.updateUser(
                        existingAdmin.copy(
                            isBlocked = false,
                            statusReason = "",
                            passwordHash = existingAdmin.passwordHash.ifBlank { "Admin123!" }
                        )
                    )
                }
                existingAdmin.id
            }
            if (existingAdmin == null && accountDao.getAccountsListByUser(adminId).isEmpty()) {
                defaultAdminAccounts(adminId).forEach { accountDao.insertAccount(it) }
            }

            // Restaura a Elena (Usuario) si no existe en una base previa o incompleta
            val existingElena = userDao.getUserByIdentifier("elena@finanzas.com")
                ?: userDao.getUserByIdentifier("elena.gomez")
            val elenaId = if (existingElena == null) {
                userDao.insertUser(defaultElenaUser())
            } else {
                if (existingElena.passwordHash.isBlank()) {
                    userDao.updateUser(existingElena.copy(passwordHash = "Elena2026!"))
                }
                existingElena.id
            }
            if (existingElena == null ||
                (accountDao.getAccountsListByUser(elenaId).isEmpty() && db.transactionDao().getTransactionsList(elenaId).isEmpty())
            ) {
                populateElenaDemoPortfolio(db, elenaId, System.currentTimeMillis())
            }

            if (db.appVersionDao().getVersionCount() == 0) {
                insertDefaultVersions(db.appVersionDao(), System.currentTimeMillis())
            }
        }

        /**
         * Población inicial de la base de datos con usuarios de prueba y cuentas reales de Nicaragua.
         * Genera las cuentas bancarias (BAC, LAFISE, Banpro, Ficohsa, Avanz, BDF) y billeteras asociadas.
         */
        suspend fun populateInitialData(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val userDao = db.userDao()
            val accountDao = db.financialAccountDao()
            val institutionDao = db.financialInstitutionDao()
            val policyDao = db.securityPolicyDao()

            // Inicialización de directiva de seguridad global por defecto
            policyDao.insertOrUpdatePolicy(defaultSecurityPolicy())

            // Catálogo inicial de Instituciones Financieras (Bancos y Billeteras) de Nicaragua
            institutionDao.insertInstitutions(defaultInstitutions())

            // Categorías por defecto del sistema
            categoryDao.insertCategories(defaultCategories())

            // Usuario de prueba: Administrador Carlos
            val adminId = userDao.insertUser(defaultAdminUser())

            // Usuario de prueba: Cliente Elena
            val demoUserId = userDao.insertUser(defaultElenaUser())


            val now = System.currentTimeMillis()
            val day = 86400000L

            // Cuentas, movimientos, presupuestos, metas y facturas de demostración para Elena
            populateElenaDemoPortfolio(db, demoUserId, now)

            // Cuentas y billeteras con formato de Nicaragua para Carlos Mendoza (Tel: 88997766)
            defaultAdminAccounts(adminId).forEach { accountDao.insertAccount(it) }

            // Inicialización de la Bitácora de Auditoría del Sistema
            val auditDao = db.auditLogDao()
            val initialLogs = listOf(
                AuditLogEntity(
                    eventType = "LOGIN_SUCCESS",
                    severity = "INFO",
                    userId = adminId,
                    username = "carlos_admin",
                    description = "Inicio de sesión administrativo exitoso",
                    ipAddress = "192.168.1.105",
                    timestamp = now - (day * 2) - 3600000L,
                    details = "Autenticación correcta con credenciales de administrador"
                ),
                AuditLogEntity(
                    eventType = "SECURITY_CHANGE",
                    severity = "WARNING",
                    userId = adminId,
                    username = "carlos_admin",
                    description = "Ajuste de directivas globales de seguridad",
                    ipAddress = "192.168.1.105",
                    timestamp = now - (day * 2),
                    details = "Configurado límite de intentos en 3 y bloqueo temporal a 300 segundos"
                ),
                AuditLogEntity(
                    eventType = "INSTITUTION_CHANGE",
                    severity = "INFO",
                    userId = adminId,
                    username = "carlos_admin",
                    description = "Actualización de catálogo de proveedores financieros",
                    ipAddress = "192.168.1.105",
                    timestamp = now - (day * 1) - 7200000L,
                    details = "Verificación de bancos y billeteras habilitadas para vinculación"
                ),
                AuditLogEntity(
                    eventType = "LOGIN_FAILED",
                    severity = "CRITICAL",
                    userId = null,
                    username = "usuario_desconocido",
                    description = "Intento fallido de inicio de sesión",
                    ipAddress = "201.218.45.12",
                    timestamp = now - 3600000L * 4,
                    details = "Intento de acceso con contraseña incorrecta (3 intentos consecutivos)"
                ),
                AuditLogEntity(
                    eventType = "LOGIN_SUCCESS",
                    severity = "INFO",
                    userId = demoUserId,
                    username = "elena_gomez",
                    description = "Inicio de sesión de usuario final exitoso",
                    ipAddress = "192.168.1.112",
                    timestamp = now - 1800000L,
                    details = "Acceso a panel de control móvil"
                )
            )
            auditDao.insertLogs(initialLogs)

            // Inicialización del Control de Versiones con versión 1.0.0 base y versión 2.0.0 obligatoria
            insertDefaultVersions(db.appVersionDao(), now)

            // Inicialización de Métricas del Monitor de APIs
            val apiMetricDao = db.apiMetricDao()
            val initialMetrics = listOf(
                ApiMetricEntity(
                    apiName = "Gemini AI Studio API",
                    endpoint = "https://generativelanguage.googleapis.com/v1beta/models",
                    latencyMs = 185,
                    isSuccess = true,
                    statusCode = 200,
                    timestamp = now - 60000L * 5
                ),
                ApiMetricEntity(
                    apiName = "BCN Official Exchange Rates API",
                    endpoint = "https://servicios.bcn.gob.ni/TcWebService",
                    latencyMs = 95,
                    isSuccess = true,
                    statusCode = 200,
                    timestamp = now - 60000L * 8
                ),
                ApiMetricEntity(
                    apiName = "Financial Institutions Gateway API",
                    endpoint = "https://api.finanziq.internal/v1/institutions/sync",
                    latencyMs = 120,
                    isSuccess = true,
                    statusCode = 200,
                    timestamp = now - 60000L * 10
                ),
                ApiMetricEntity(
                    apiName = "Push Notification & Bill Reminders API",
                    endpoint = "https://api.finanziq.internal/v1/bills/notify",
                    latencyMs = 78,
                    isSuccess = true,
                    statusCode = 200,
                    timestamp = now - 60000L * 12
                )
            )
            apiMetricDao.insertMetrics(initialMetrics)
        }

        private fun defaultSecurityPolicy() = SecurityPolicyEntity(
            id = 1,
            maxLoginAttempts = 3,
            lockoutDurationSeconds = 300,
            passwordMinLength = 8,
            passwordMaxLength = 20,
            requireUppercase = true,
            requireLowercase = false,
            requireNumber = true,
            requireSpecialChar = true,
            minAge = 18,
            maxNameLength = 70,
            requireCapitalizedNames = true
        )

        private fun defaultInstitutions() = listOf(
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "BANPRO Grupo Promerica", abbreviation = "BANPRO", type = "BANCO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "BAC Credomatic Nicaragua", abbreviation = "BAC", type = "BANCO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Banco LAFISE BANCENTRO", abbreviation = "LAFISE", type = "BANCO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Banco de Finanzas (BDF)", abbreviation = "BDF", type = "BANCO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Banco FICOHSA Nicaragua", abbreviation = "FICOHSA", type = "BANCO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Banco AVANZ", abbreviation = "AVANZ", type = "BANCO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Billetera Móvil Banpro", abbreviation = "Banpro Móvil", type = "BILLETERA", supportedBankBrands = "BANPRO", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Billetera Móvil LAFISE", abbreviation = "LAFISE Móvil", type = "BILLETERA", supportedBankBrands = "LAFISE", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Kash BAC Nicaragua", abbreviation = "Kash BAC", type = "BILLETERA", supportedBankBrands = "BAC", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Billetera Móvil BDF", abbreviation = "BDF Móvil", type = "BILLETERA", supportedBankBrands = "BDF", isActive = true),
            FinancialInstitutionEntity(country = "Nicaragua", fullName = "Billetera Móvil Ficohsa", abbreviation = "Ficohsa Móvil", type = "BILLETERA", supportedBankBrands = "FICOHSA", isActive = true)
        )

        private fun defaultCategories() = listOf(
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

        private fun defaultAdminUser() = UserEntity(
            firstName = "Carlos",
            lastName = "Mendoza",
            username = "admin",
            age = 32,
            phone = "88997766",
            email = "admin@finanzas.com",
            passwordHash = "Admin123!",
            role = "ADMIN"
        )

        private fun defaultElenaUser() = UserEntity(
            firstName = "Elena",
            lastName = "Gómez",
            username = "elena.gomez",
            age = 26,
            phone = "77889900",
            email = "elena@finanzas.com",
            passwordHash = "Elena2026!",
            role = "USER"
        )

        private fun defaultAdminAccounts(adminId: Long) = listOf(
            FinancialAccountEntity(userId = adminId, toolType = "CUENTA", bankName = "Ficohsa", accountType = "DEBITO", debitSubType = "CORRIENTE", accountNumber = "501-887766-2", balance = 45000.0, isActive = true),
            FinancialAccountEntity(userId = adminId, toolType = "CUENTA", bankName = "Avanz", accountType = "DEBITO", debitSubType = "AHORRO", accountNumber = "601-332211-8", balance = 22300.0, isActive = true),
            FinancialAccountEntity(userId = adminId, toolType = "CUENTA", bankName = "BDF", accountType = "CREDITO", debitSubType = "", accountNumber = "701-445566-0", creditLimit = 50000.0, balance = 12000.0, isActive = true),
            FinancialAccountEntity(userId = adminId, toolType = "BILLETERA", bankName = "Ficohsa", accountType = "BILLETERA", debitSubType = "", accountNumber = "88997766", balance = 5000.0, associatedPhone = "88997766", linkedBank = "Ficohsa", linkedAccountType = "DEBITO", linkedDebitSubType = "CORRIENTE", linkedAccountNumber = "501-887766-2", isActive = true)
        )

        private suspend fun populateElenaDemoPortfolio(db: AppDatabase, demoUserId: Long, now: Long) {
            val day = 86400000L
            val accountDao = db.financialAccountDao()
            val transactionDao = db.transactionDao()
            val budgetDao = db.budgetDao()
            val goalDao = db.savingsGoalDao()
            val billDao = db.billReminderDao()

            val elenaAccounts = listOf(
                FinancialAccountEntity(userId = demoUserId, toolType = "CUENTA", bankName = "BAC", accountType = "DEBITO", debitSubType = "AHORRO", accountNumber = "001-987654-3", balance = 14200.0, isActive = true),
                FinancialAccountEntity(userId = demoUserId, toolType = "CUENTA", bankName = "LAFISE", accountType = "DEBITO", debitSubType = "NOMINA", accountNumber = "204-554433-1", balance = 9800.0, isActive = true),
                FinancialAccountEntity(userId = demoUserId, toolType = "CUENTA", bankName = "Banpro", accountType = "CREDITO", debitSubType = "", accountNumber = "402-112233-9", creditLimit = 35000.0, balance = 18500.0, isActive = true),
                FinancialAccountEntity(userId = demoUserId, toolType = "BILLETERA", bankName = "Banpro", accountType = "BILLETERA", debitSubType = "", accountNumber = "77889900", balance = 3450.0, associatedPhone = "77889900", linkedBank = "Banpro", linkedAccountType = "DEBITO", linkedDebitSubType = "AHORRO", linkedAccountNumber = "402-998877-0", isActive = true),
                FinancialAccountEntity(userId = demoUserId, toolType = "BILLETERA", bankName = "BAC", accountType = "BILLETERA", debitSubType = "", accountNumber = "77889900", balance = 1200.0, associatedPhone = "77889900", linkedBank = "BAC", linkedAccountType = "DEBITO", linkedDebitSubType = "AHORRO", linkedAccountNumber = "001-987654-3", isActive = true)
            )
            elenaAccounts.forEach { accountDao.insertAccount(it) }

            if (transactionDao.getTransactionsList(demoUserId).isEmpty()) {
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
            }

            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Alimentación", monthlyLimit = 5000.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Transporte", monthlyLimit = 2000.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Entretenimiento", monthlyLimit = 1500.0, monthYear = "2026-09"))
            budgetDao.insertBudget(BudgetEntity(userId = demoUserId, category = "Servicios Básicos", monthlyLimit = 2200.0, monthYear = "2026-09"))

            goalDao.insertGoal(SavingsGoalEntity(userId = demoUserId, title = "Fondo de Emergencia", targetAmount = 30000.0, currentAmount = 14500.0, targetDateMillis = now + (day * 120), iconName = "shield"))
            goalDao.insertGoal(SavingsGoalEntity(userId = demoUserId, title = "Viaje de Vacaciones", targetAmount = 15000.0, currentAmount = 8200.0, targetDateMillis = now + (day * 90), iconName = "flight"))

            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Recibo de Agua Potable", amount = 380.0, dueDateMillis = now + (day * 3), category = "Servicios Básicos", isPaid = false))
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Internet de Fibra Óptica", amount = 950.0, dueDateMillis = now + (day * 7), category = "Servicios Básicos", isPaid = false))
            billDao.insertBill(BillReminderEntity(userId = demoUserId, title = "Cuota Préstamo Personal", amount = 2500.0, dueDateMillis = now + (day * 15), category = "Otros Gastos", isPaid = false))
        }

        private suspend fun insertDefaultVersions(versionDao: AppVersionDao, now: Long) {
            val day = 86400000L
            versionDao.insertVersion(
                AppVersionEntity(
                    versionNumber = "1.0.0",
                    releaseNotes = "• Versión inicial estable de FinanzIQ\n• Gestión de transacciones, presupuestos y metas\n• Catálogo bancario y asistente de IA integrado",
                    isMandatory = false,
                    publishedBy = "Admin",
                    releaseDate = now - (day * 15)
                )
            )
            versionDao.insertVersion(
                AppVersionEntity(
                    versionNumber = "2.0.0",
                    releaseNotes = "• Actualización obligatoria del sistema FinanzIQ\n• Sincronización global de preferencias y tasas de cambio oficiales BCN\n• Nuevas directivas de seguridad bancaria y modo oscuro homogéneo",
                    isMandatory = true,
                    publishedBy = "Carlos Mendoza (Admin)",
                    releaseDate = now - (day * 1)
                )
            )
        }
    }
}

