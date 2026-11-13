package com.example

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.ui.FinanceViewModel
import com.example.util.CountryPhoneData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabasePersistenceRobolectricTest {

    private fun resetSingletonForTest() {
        val field = AppDatabase::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        val existing = field.get(null) as? AppDatabase
        runCatching { existing?.close() }
        field.set(null, null)
    }

    private fun awaitCondition(timeoutMs: Long = 5_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(25)
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Se agotó el tiempo esperando la condición de prueba", condition())
    }

    @Test
    fun `stale v5 database file upgrades cleanly to v6 and allows demo logins`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        resetSingletonForTest()
        app.deleteDatabase("finanzas_inteligentes.db")

        // Crea manualmente una base de datos vieja en versión 5 con columnas incompletas
        val dbFile = app.getDatabasePath("finanzas_inteligentes.db")
        dbFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { legacyDb ->
            legacyDb.execSQL(
                "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, firstName TEXT NOT NULL, lastName TEXT NOT NULL, age INTEGER NOT NULL, phone TEXT NOT NULL, email TEXT NOT NULL, passwordHash TEXT NOT NULL, role TEXT NOT NULL)"
            )
            legacyDb.version = 5
        }

        val (repairedDb, _) = AppDatabase.ensureSeededOrRecover(app)
        val elena = repairedDb.userDao().getUserByIdentifier("elena@finanzas.com")
        val carlos = repairedDb.userDao().getUserByIdentifier("admin@finanzas.com")

        assertNotNull("Elena debe cargarse tras actualizar de v5 a v6", elena)
        assertNotNull("Carlos debe cargarse tras actualizar de v5 a v6", carlos)

        // Verifica que el ViewModel inicia sesión con Carlos sin errores
        val viewModel = FinanceViewModel(app)
        var loginOk = false
        viewModel.login("admin@finanzas.com", "Admin123!") { result -> loginOk = result }
        awaitCondition {
            viewModel.currentUser.value != null || viewModel.authError.value != null
        }
        assertNull("No debe haber error de login", viewModel.authError.value)
        assertTrue("El login debe completarse correctamente", loginOk)
        assertEquals("ADMIN", viewModel.currentUser.value?.role)
    }

    @Test
    fun `corrupted same-version schema recovers automatically and restores demo users`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        resetSingletonForTest()
        app.deleteDatabase("finanzas_inteligentes.db")

        // Simula un archivo corrupto ya marcado con versión 6 pero sin el esquema correcto
        val dbFile = app.getDatabasePath("finanzas_inteligentes.db")
        dbFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { brokenDb ->
            brokenDb.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY NOT NULL, firstName TEXT NOT NULL)")
            brokenDb.version = 6
        }

        val (recoveredDb, wasRecovered) = AppDatabase.ensureSeededOrRecover(app)
        assertTrue("Debe detectar el esquema incompatible y reconstruir la base", wasRecovered)
        assertNotNull(recoveredDb.userDao().getUserByIdentifier("elena@finanzas.com"))
        assertNotNull(recoveredDb.userDao().getUserByIdentifier("admin@finanzas.com"))
    }

    @Test
    fun `registering new user and saving movement persist in SQLite and allow re-login`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        resetSingletonForTest()
        app.deleteDatabase("finanzas_inteligentes.db")

        val viewModel = FinanceViewModel(app)
        var registerSuccess = false
        viewModel.register(
            firstNames = "Kenneth ",
            lastNames = "Hernández ",
            username = "kenn0682",
            ageStr = "22",
            phone = "88442737",
            countryConfig = CountryPhoneData.americanCountries.first(),
            email = "kenneth@finanzas.com",
            password = "Password1!",
            confirmPassword = "Password1!",
            preferredCurrency = CountryPhoneData.americanCurrencies.first(),
            role = "USER",
            onSuccess = { registerSuccess = true }
        )

        awaitCondition {
            registerSuccess || viewModel.authError.value != null
        }

        assertNull("No debe haber error en registro", viewModel.authError.value)
        assertTrue("El registro debe completarse con éxito", registerSuccess)
        val registeredUser = viewModel.currentUser.value
        assertNotNull("El usuario recién registrado debe quedar en sesión", registeredUser)
        assertEquals("kenn0682", registeredUser?.username)

        // Guarda un movimiento para comprobar persistencia real en SQLite
        viewModel.addMovement(
            title = "Salario Mensual",
            amount = 12500.0,
            type = "INCOME",
            categoryGroup = "INGRESO_ACTIVO",
            category = "Salario"
        )

        awaitCondition {
            runBlocking(Dispatchers.IO) {
                viewModel.database.transactionDao().getTransactionsList(registeredUser!!.id).isNotEmpty()
            }
        }

        val savedTxs = kotlinx.coroutines.withContext(Dispatchers.IO) {
            viewModel.database.transactionDao().getTransactionsList(registeredUser!!.id)
        }
        assertEquals(1, savedTxs.size)
        assertEquals("Salario Mensual", savedTxs.first().title)

        // Cierra sesión y vuelve a entrar con el usuario registrado
        viewModel.logout()
        assertNull(viewModel.currentUser.value)

        var reloginOk = false
        viewModel.login("kenn0682", "Password1!") { ok -> reloginOk = ok }
        awaitCondition {
            viewModel.currentUser.value != null || viewModel.authError.value != null
        }
        assertTrue("El usuario registrado debe poder volver a iniciar sesión", reloginOk)
        assertEquals("kenneth@finanzas.com", viewModel.currentUser.value?.email)
    }
}
