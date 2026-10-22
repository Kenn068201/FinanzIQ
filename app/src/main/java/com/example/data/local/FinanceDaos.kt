package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :identifier OR username = :identifier LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateMillis DESC")
    fun getTransactionsByUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsByDateRange(userId: Long, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getTransactionsList(userId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId AND monthYear = :monthYear")
    fun getBudgetsByUserAndMonth(userId: Long, monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgetsByUser(userId: Long): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY targetDateMillis ASC")
    fun getGoalsByUser(userId: Long): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)
}

@Dao
interface BillReminderDao {
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId ORDER BY dueDateMillis ASC")
    fun getBillsByUser(userId: Long): Flow<List<BillReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillReminderEntity): Long

    @Update
    suspend fun updateBill(bill: BillReminderEntity)

    @Delete
    suspend fun deleteBill(bill: BillReminderEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}

/**
 * DAO para la gestión de cuentas bancarias y billeteras digitales.
 * Proporciona consultas reactivas con Flow y operaciones de inserción y actualización
 * para alternar el estado activo/deshabilitado de las herramientas financieras.
 */
@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts WHERE userId = :userId ORDER BY isActive DESC, id ASC")
    fun getAccountsByUser(userId: Long): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE userId = :userId")
    suspend fun getAccountsListByUser(userId: Long): List<FinancialAccountEntity>

    @Query("SELECT * FROM financial_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): FinancialAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccountEntity): Long

    @Update
    suspend fun updateAccount(account: FinancialAccountEntity)

    @Delete
    suspend fun deleteAccount(account: FinancialAccountEntity)

    @Query("UPDATE financial_accounts SET isActive = :isActive WHERE id = :id")
    suspend fun setAccountActiveStatus(id: Long, isActive: Boolean)
}

/**
 * DAO para la administración del catálogo de Instituciones Financieras (Bancos y Billeteras).
 * Permite al administrador consultar, agregar, desactivar con motivo, reactivar y eliminar entidades.
 * Proporciona Flows reactivos para que los cambios se reflejen de inmediato tanto en la interfaz de
 * administración como en la interfaz de vinculación de cuentas del usuario final.
 */
@Dao
interface FinancialInstitutionDao {
    @Query("SELECT * FROM financial_institutions ORDER BY type ASC, fullName ASC")
    fun getAllInstitutions(): Flow<List<FinancialInstitutionEntity>>

    @Query("SELECT * FROM financial_institutions WHERE isActive = 1 ORDER BY type ASC, fullName ASC")
    fun getActiveInstitutions(): Flow<List<FinancialInstitutionEntity>>

    @Query("SELECT * FROM financial_institutions WHERE id = :id LIMIT 1")
    suspend fun getInstitutionById(id: Long): FinancialInstitutionEntity?

    @Query("SELECT * FROM financial_institutions WHERE fullName = :fullName LIMIT 1")
    suspend fun getInstitutionByName(fullName: String): FinancialInstitutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitution(institution: FinancialInstitutionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitutions(institutions: List<FinancialInstitutionEntity>)

    @Update
    suspend fun updateInstitution(institution: FinancialInstitutionEntity)

    @Delete
    suspend fun deleteInstitution(institution: FinancialInstitutionEntity)
}

/**
 * DAO para la persistencia de las Directivas de Seguridad Globales de la aplicación.
 * Garantiza que las modificaciones realizadas por el administrador persistan en la base de datos
 * y se propaguen mediante Flow a todos los flujos de validación del sistema (login, registro, contraseñas).
 */
@Dao
interface SecurityPolicyDao {
    @Query("SELECT * FROM security_policies WHERE id = 1 LIMIT 1")
    fun getSecurityPolicy(): Flow<SecurityPolicyEntity?>

    @Query("SELECT * FROM security_policies WHERE id = 1 LIMIT 1")
    suspend fun getSecurityPolicyDirect(): SecurityPolicyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePolicy(policy: SecurityPolicyEntity)
}


