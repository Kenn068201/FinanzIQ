package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FinanzIQ", appName)
  }

  @Test
  fun `launch MainActivity and verify user and admin flows without crash`() {
    org.robolectric.shadows.ShadowLog.stream = System.out
    composeTestRule.waitForIdle()

    // 1. Iniciar sesión como usuario de prueba Elena
    composeTestRule.onNodeWithTag("demo_user_button")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    // Esperar a que cargue la DB asíncrona y el diálogo obligatorio si aparece
    try {
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule.onAllNodesWithTag("nav_tab_dashboard").fetchSemanticsNodes().isNotEmpty() ||
          composeTestRule.onAllNodesWithTag("btn_confirm_app_update").fetchSemanticsNodes().isNotEmpty() ||
          composeTestRule.onAllNodesWithTag("login_error_box").fetchSemanticsNodes().isNotEmpty()
      }
    } catch (e: Throwable) {
      println("ROBOLECTRIC_TREE_DUMP:\n" + composeTestRule.onRoot(useUnmergedTree = true).printToString())
      throw e
    }

    // Si aparece el botón de actualización obligatoria, presionarlo
    if (composeTestRule.onAllNodesWithTag("btn_confirm_app_update").fetchSemanticsNodes().isNotEmpty()) {
      composeTestRule.onNodeWithTag("btn_confirm_app_update").performClick()
      composeTestRule.waitForIdle()
    }

    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule.onAllNodesWithTag("nav_tab_dashboard").fetchSemanticsNodes().isNotEmpty()
    }

    // Recorrer pestañas del usuario final
    listOf("transactions", "budgets", "analytics", "savings", "bills", "ai_assistant", "dashboard").forEach { route ->
      composeTestRule.onNodeWithTag("nav_tab_$route").performClick()
      composeTestRule.waitForIdle()
    }

    // Abrir diálogo de Exportar PDF en Movimientos y verificar que abre sin errores
    composeTestRule.onNodeWithTag("nav_tab_transactions").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("export_report_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("generate_pdf_report_button").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("generate_pdf_report_button").performClick()
    composeTestRule.waitForIdle()

    // Abrir Ajustes y Preferencias y guardar sin fallos
    composeTestRule.onNodeWithTag("user_profile_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("menu_settings_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("settings_save_btn").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("settings_save_btn").performClick()
    composeTestRule.waitForIdle()

    // Cerrar sesión
    composeTestRule.onNodeWithTag("user_profile_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("logout_button").performClick()
    composeTestRule.waitForIdle()

    // 2. Iniciar sesión como administrador Carlos (desplazando hasta el botón antes de pulsar)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("demo_admin_button").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("demo_admin_button")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    try {
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule.onAllNodesWithTag("nav_tab_admin").fetchSemanticsNodes().isNotEmpty()
      }
    } catch (e: Throwable) {
      println("ROBOLECTRIC_ADMIN_TREE_DUMP:\n" + composeTestRule.onRoot(useUnmergedTree = true).printToString())
      throw e
    }
    composeTestRule.onNodeWithTag("nav_tab_admin").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("search_institutions_input").fetchSemanticsNodes().isNotEmpty()
    }
  }

  @Test
  fun `demo users recover and log in even when existing database lacks demo rows`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getDatabase(
      context,
      kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    )
    kotlinx.coroutines.runBlocking {
      // Simula una base previa donde existe otro usuario pero faltan Elena y Carlos
      db.ensureSeeded()
      db.userDao().getUserByIdentifier("elena@finanzas.com")?.let { db.userDao().deleteUser(it) }
      db.userDao().getUserByIdentifier("admin@finanzas.com")?.let { db.userDao().deleteUser(it) }
      db.userDao().insertUser(
        com.example.data.local.UserEntity(
          firstName = "Usuario",
          lastName = "Previo",
          username = "usuario.previo",
          age = 25,
          phone = "88112233",
          email = "previo@finanzas.com",
          passwordHash = "Previo123!",
          role = "USER"
        )
      )

      db.ensureSeeded()
      val restoredElena = db.userDao().getUserByIdentifier("elena@finanzas.com")
      val restoredAdmin = db.userDao().getUserByIdentifier("admin@finanzas.com")
      val caseInsensitiveElena = db.userDao().getUserByIdentifier("elena@finanzas.com")
      org.junit.Assert.assertNotNull("Elena debe restaurarse automáticamente", restoredElena)
      org.junit.Assert.assertNotNull("Carlos Admin debe restaurarse automáticamente", restoredAdmin)
      org.junit.Assert.assertEquals("Elena2026!", caseInsensitiveElena?.passwordHash)
      org.junit.Assert.assertEquals("Admin123!", restoredAdmin?.passwordHash)
      org.junit.Assert.assertEquals(false, restoredAdmin?.isBlocked)
    }
  }
}
