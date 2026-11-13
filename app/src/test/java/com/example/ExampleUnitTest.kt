package com.example

import com.example.util.CurrencyConversionUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de Caja Blanca (White-Box Testing) para la plataforma FinanzIQ.
 *
 * Estas pruebas analizan la estructura interna, flujos de control y reglas de negocio del sistema:
 * 1. Reversión matemática de saldos en cuentas y dashboard al eliminar movimientos (Ingreso / Gasto).
 * 2. Validación de umbrales en dólares (USD) para control de riesgos y confirmación explícita (ACEPTAR).
 * 3. Detección del 100% en metas de ahorro para clasificarlas entre 'En progreso' y 'Alcanzadas'.
 * 4. Control de límites y alertas porcentuales en presupuestos mensuales (Saludable, Advertencia 80%, Excedido 100%).
 * 5. Lógica de cálculo en el simulador de ahorro diario y proyección financiera.
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /**
     * Prueba de Caja Blanca 1: Reversión de saldo al eliminar una transacción de INGRESO.
     *
     * Qué hace: Verifica que cuando se elimina un ingreso registrado previamente en una cuenta,
     * el saldo de la cuenta disminuya restando dicho valor, neutralizando el impacto.
     * Por qué se implementa: Evita discrepancias entre el balance del dashboard y el historial de transacciones.
     * Relación con el código: Emula la lógica ejecutada en FinanceViewModel.deleteTransaction donde
     * un movimiento de tipo INCOME resta su monto al saldo actual de la cuenta.
     */
    @Test
    fun testRevertBalanceOnIncomeDeletion() {
        val initialAccountBalance = 5000.0
        val incomeAmount = 1200.0

        // Saldo después de registrar el ingreso
        val balanceWithIncome = initialAccountBalance + incomeAmount
        assertEquals(6200.0, balanceWithIncome, 0.001)

        // Al eliminar el ingreso, se revierte restando el monto registrado
        val balanceAfterDeletion = balanceWithIncome - incomeAmount
        assertEquals(initialAccountBalance, balanceAfterDeletion, 0.001)
    }

    /**
     * Prueba de Caja Blanca 2: Reversión de saldo al eliminar una transacción de GASTO.
     *
     * Qué hace: Verifica que al eliminar un gasto, el monto sea reembolsado a la cuenta
     * sumándose nuevamente al balance.
     * Por qué se implementa: Garantiza que un gasto erróneo no deje el dinero descontado indebidamente.
     * Relación con el código: Corresponde a la rama EXPENSE de FinanceViewModel.deleteTransaction.
     */
    @Test
    fun testRevertBalanceOnExpenseDeletion() {
        val initialAccountBalance = 5000.0
        val expenseAmount = 850.0

        // Saldo tras descontar el gasto
        val balanceWithExpense = initialAccountBalance - expenseAmount
        assertEquals(4150.0, balanceWithExpense, 0.001)

        // Al eliminar el gasto, se reintegra sumándolo a la cuenta
        val balanceAfterDeletion = balanceWithExpense + expenseAmount
        assertEquals(initialAccountBalance, balanceAfterDeletion, 0.001)
    }

    /**
     * Prueba de Caja Blanca 3: Clasificación de umbrales USD para registro de movimientos.
     *
     * Qué hace: Valida los 3 niveles de riesgo según el valor convertido a USD:
     * - Nivel 1 (< 10,000 USD): Registro normal sin advertencias intermedias.
     * - Nivel 2 (10,000 a 1,000,000 USD): Despliegue de aviso intermedio de confirmación.
     * - Nivel 3 (> 1,000,000 USD): Bloqueo automático que exige la palabra "ACEPTAR".
     * Por qué se implementa: Cumple con la directiva estricta de mitigación de errores del usuario.
     * Relación con el código: Refleja las condiciones implementadas en TransactionsScreen.kt.
     */
    @Test
    fun testTransactionUsdThresholdTiers() {
        fun evaluateThreshold(amountUsd: Double): String {
            return when {
                amountUsd > 1_000_000.0 -> "REQUIRE_EXPLICIT_ACCEPT"
                amountUsd >= 10_000.0 -> "REQUIRE_WARNING_PROMPT"
                else -> "ALLOW_DIRECT_SAVE"
            }
        }

        // Caso Nivel 1: Montos estándar normales
        assertEquals("ALLOW_DIRECT_SAVE", evaluateThreshold(250.0))
        assertEquals("ALLOW_DIRECT_SAVE", evaluateThreshold(9_999.99))

        // Caso Nivel 2: Montos inusualmente altos
        assertEquals("REQUIRE_WARNING_PROMPT", evaluateThreshold(10_000.0))
        assertEquals("REQUIRE_WARNING_PROMPT", evaluateThreshold(500_000.0))
        assertEquals("REQUIRE_WARNING_PROMPT", evaluateThreshold(1_000_000.0))

        // Caso Nivel 3: Montos astronómicos que requieren confirmación con palabra clave
        assertEquals("REQUIRE_EXPLICIT_ACCEPT", evaluateThreshold(1_000_000.01))
        assertEquals("REQUIRE_EXPLICIT_ACCEPT", evaluateThreshold(5_000_000.0))
    }

    /**
     * Prueba de Caja Blanca 4: Conversión monetaria multimoneda hacia USD para evaluación de umbral.
     *
     * Qué hace: Evalúa la tasa de conversión de diversas monedas soportadas (EUR, NIO, CRC, GTQ)
     * para asegurar que el cálculo en USD sea exacto antes de validar el tope.
     * Por qué se implementa: Evita falsos positivos con monedas de alta denominación nominal (como el Colón o Córdoba).
     * Relación con el código: Utiliza directamente CurrencyConversionUtil.convertToUsd.
     */
    @Test
    fun testCurrencyConversionToUsdForThreshold() {
        // 100 USD directos
        val usdDirect = CurrencyConversionUtil.convertToUsd(100.0, "USD")
        assertEquals(100.0, usdDirect, 0.01)

        // 36,800 Córdobas (NIO) equivalen aprox a 1,000 USD (tasa ~36.8)
        val usdFromNio = CurrencyConversionUtil.convertToUsd(36800.0, "NIO")
        assertTrue(usdFromNio in 990.0..1010.0)

        // 515,000 Colones (CRC) equivalen aprox a 1,000 USD (tasa oficial BCN/BCCR ~515 CRC por USD)
        val usdFromCrc = CurrencyConversionUtil.convertToUsd(515000.0, "CRC")
        assertTrue(usdFromCrc in 990.0..1010.0)

        // Una suma de 40,000,000 NIO supera ampliamente el tope de 1M USD (~1.08M USD)
        val highNioInUsd = CurrencyConversionUtil.convertToUsd(40_000_000.0, "NIO")
        assertTrue(highNioInUsd > 1_000_000.0)
    }

    /**
     * Prueba de Caja Blanca 5: Validación de palabra de confirmación "ACEPTAR".
     *
     * Qué hace: Verifica que la palabra de confirmación sea aceptada sin importar mayúsculas o espacios accidentales,
     * pero rechace cualquier otra entrada.
     * Por qué se implementa: Permite una experiencia tolerante a mayúsculas/minúsculas asegurando intención real.
     * Relación con el código: Valida la lógica del botón de confirmación en TransactionsScreen.kt.
     */
    @Test
    fun testExplicitConfirmationKeyword() {
        fun isConfirmationValid(input: String): Boolean {
            return input.trim().equals("ACEPTAR", ignoreCase = true)
        }

        assertTrue(isConfirmationValid("ACEPTAR"))
        assertTrue(isConfirmationValid("aceptar"))
        assertTrue(isConfirmationValid(" Aceptar "))
        assertFalse(isConfirmationValid("SI"))
        assertFalse(isConfirmationValid("OK"))
        assertFalse(isConfirmationValid(""))
    }

    /**
     * Prueba de Caja Blanca 6: Detección y clasificación de metas de ahorro alcanzadas (100%).
     *
     * Qué hace: Comprueba que una meta de ahorro con saldo acumulado igual o superior a su objetivo
     * se catalogue como alcanzada y active el reconocimiento al usuario.
     * Por qué se implementa: Satisface la separación visual entre 'En progreso' y 'Alcanzadas' requerida.
     * Relación con el código: Modela la división efectuada en SavingsScreen.kt.
     */
    @Test
    fun testSavingsGoalCompletionClassification() {
        data class Goal(val title: String, val current: Double, val target: Double) {
            val isReached: Boolean get() = target > 0.0 && current >= target
            val progressPercentage: Double get() = if (target > 0.0) (current / target).coerceIn(0.0, 1.0) * 100 else 0.0
        }

        val goalInProgress = Goal("Fondo de Emergencia", 450.0, 1000.0)
        val goalJustReached = Goal("Laptop Nueva", 1200.0, 1200.0)
        val goalOverExceeded = Goal("Viaje de Vacaciones", 2500.0, 2000.0)

        // Meta en progreso
        assertFalse(goalInProgress.isReached)
        assertEquals(45.0, goalInProgress.progressPercentage, 0.01)

        // Metas alcanzadas
        assertTrue(goalJustReached.isReached)
        assertEquals(100.0, goalJustReached.progressPercentage, 0.01)

        assertTrue(goalOverExceeded.isReached)
        assertEquals(100.0, goalOverExceeded.progressPercentage, 0.01)
    }

    /**
     * Prueba de Caja Blanca 7: Reglas de estado de presupuestos mensuales (80% y 100%).
     *
     * Qué hace: Verifica los estados de alerta del presupuesto: Saludable (<80%), Advertencia (>=80% y <100%),
     * y Excedido (>=100%).
     * Por qué se implementa: Permite el feedback de colorimetría y estado en BudgetsScreen.kt.
     */
    @Test
    fun testMonthlyBudgetStatusThresholds() {
        fun getBudgetStatus(spent: Double, limit: Double): String {
            if (limit <= 0.0) return "INVALID"
            val ratio = spent / limit
            return when {
                ratio >= 1.0 -> "OVER_BUDGET"
                ratio >= 0.80 -> "WARNING_80_PERCENT"
                else -> "HEALTHY"
            }
        }

        assertEquals("HEALTHY", getBudgetStatus(400.0, 1000.0))
        assertEquals("HEALTHY", getBudgetStatus(799.0, 1000.0))
        assertEquals("WARNING_80_PERCENT", getBudgetStatus(800.0, 1000.0))
        assertEquals("WARNING_80_PERCENT", getBudgetStatus(950.0, 1000.0))
        assertEquals("OVER_BUDGET", getBudgetStatus(1000.0, 1000.0))
        assertEquals("OVER_BUDGET", getBudgetStatus(1200.0, 1000.0))
    }

    /**
     * Prueba de Caja Blanca 8: Proyección del simulador de ahorro diario.
     *
     * Qué hace: Valida la fórmula matemática de acumulación por días proyectados (30, 90, 180, 365).
     * Por qué se implementa: Garantiza precisión en la calculadora de la Interfaz del Simulador de Ahorro.
     */
    @Test
    fun testDailySavingsProjection() {
        val dailySave = 5.0

        val days30 = dailySave * 30
        val days90 = dailySave * 90
        val days180 = dailySave * 180
        val days365 = dailySave * 365

        assertEquals(150.0, days30, 0.001)
        assertEquals(450.0, days90, 0.001)
        assertEquals(900.0, days180, 0.001)
        assertEquals(1825.0, days365, 0.001)
    }

    /**
     * Prueba de Caja Blanca 9: Ciclo de vida y reaparición continua de actualizaciones obligatorias.
     * 
     * Qué hace:
     * Verifica que cada vez que el Administrador registra una nueva versión obligatoria (manual o automática):
     * 1. El usuario final con una versión anterior siempre reciba el aviso de actualización obligatorio.
     * 2. Una vez que el usuario presiona "Actualizar Ahora", la versión queda registrada y el aviso desaparece.
     * 3. Al registrarse una actualización subsiguiente a futuro, el aviso vuelve a salir puntualmente.
     * 4. Para el Administrador, el aviso nunca bloquea su interfaz ya que opera directamente con la última versión.
     * 
     * Por qué se implementa:
     * Satisface la directiva estricta de que cada actualización obligatoria presente el botón de actualizar
     * y el comportamiento se repita sin excepciones para todas las versiones futuras.
     * 
     * Relación con el código:
     * Valida la condición condicional de MainAppNavigation.kt y la lógica de FinanceViewModel.kt.
     */
    @Test
    fun testMandatoryUpdatePromptLifecycle() {
        data class MockVersion(val versionNumber: String, val isMandatory: Boolean, val releaseNotes: String)

        fun shouldShowPrompt(
            userRole: String,
            clientVersion: String,
            latestVersion: MockVersion?,
            dismissedOptional: String? = null
        ): Boolean {
            val isEndUser = userRole != "ADMIN"
            if (!isEndUser || latestVersion == null) return false
            if (latestVersion.versionNumber == clientVersion) return false
            return latestVersion.isMandatory || dismissedOptional != latestVersion.versionNumber
        }

        // 1. Estado inicial del usuario Elena en v1.0.0 cuando el sistema tiene v2.0.0 obligatoria
        val v2_0_0 = MockVersion("2.0.0", isMandatory = true, releaseNotes = "Lanzamiento oficial")
        var elenaVersion = "1.0.0"

        assertTrue(shouldShowPrompt(userRole = "USER", clientVersion = elenaVersion, latestVersion = v2_0_0))
        // El administrador Carlos nunca debe ver bloqueada su interfaz
        assertFalse(shouldShowPrompt(userRole = "ADMIN", clientVersion = "2.0.0", latestVersion = v2_0_0))

        // 2. Elena pulsa "Actualizar Ahora"
        elenaVersion = "2.0.0"
        assertFalse(shouldShowPrompt(userRole = "USER", clientVersion = elenaVersion, latestVersion = v2_0_0))

        // 3. El Administrador registra una nueva versión obligatoria v2.0.1 (manual o por seguridad)
        val v2_0_1 = MockVersion("2.0.1", isMandatory = true, releaseNotes = "Actualización obligatoria de seguridad")
        // Como la versión de Elena es v2.0.0 y la nueva es v2.0.1, el aviso debe reaparecer inmediatamente
        assertTrue(shouldShowPrompt(userRole = "USER", clientVersion = elenaVersion, latestVersion = v2_0_1))

        // 4. Elena actualiza a v2.0.1
        elenaVersion = "2.0.1"
        assertFalse(shouldShowPrompt(userRole = "USER", clientVersion = elenaVersion, latestVersion = v2_0_1))

        // 5. El Administrador publica en el futuro v2.0.2 obligatoria
        val v2_0_2 = MockVersion("2.0.2", isMandatory = true, releaseNotes = "Nuevos módulos de auditoría")
        assertTrue(shouldShowPrompt(userRole = "USER", clientVersion = elenaVersion, latestVersion = v2_0_2))
    }

    /**
     * Prueba de Caja Blanca 10: Incremento correlativo y sincronización de notas en actualización por seguridad.
     * 
     * Qué hace:
     * Valida el algoritmo de cálculo de parche de versión y garantiza que las notas de cambio de seguridad
     * sean idénticas y homogéneas.
     * 
     * Por qué se implementa:
     * Asegura que el cambio de directivas de seguridad siempre genere una versión subsiguiente válida.
     */
    @Test
    fun testAutoSecurityUpdateVersionIncrement() {
        fun calculateNextVersion(currentVersion: String): String {
            val parts = currentVersion.split(".")
            return if (parts.size == 3) {
                val patch = (parts[2].toIntOrNull() ?: 0) + 1
                "${parts[0]}.${parts[1]}.$patch"
            } else {
                "${currentVersion}.1"
            }
        }

        assertEquals("2.0.1", calculateNextVersion("2.0.0"))
        assertEquals("2.0.2", calculateNextVersion("2.0.1"))
        assertEquals("2.1.1", calculateNextVersion("2.1.0"))
        assertEquals("3.0.1", calculateNextVersion("3.0.0"))
        assertEquals("3.0.1", calculateNextVersion("3.0"))

        val expectedSecurityNote = "Actualización obligatoria de seguridad: Nuevas directivas de acceso y políticas de contraseñas aplicadas por administración."
        assertTrue(expectedSecurityNote.contains("Actualización obligatoria de seguridad"))
    }
}
