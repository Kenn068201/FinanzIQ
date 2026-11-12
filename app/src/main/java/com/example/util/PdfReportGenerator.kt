package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.FinancialAccountEntity
import com.example.data.local.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador de Informes Financieros en formato PDF Real.
 * Genera un documento PDF estructurado en 2 tablas principales:
 *  1. Tabla de Balance General: Resumen de Ingresos, Gastos y Balance Neto.
 *  2. Tabla Desglosada: Flujo y Saldos por cada Billetera y Cuenta Vinculada.
 */
object PdfReportGenerator {

    /**
     * Genera un archivo PDF estructurado con 2 tablas completas y retorna el archivo generado.
     */
    fun generateFinancialReportPdf(
        context: Context,
        startDateMillis: Long,
        endDateMillis: Long,
        transactions: List<TransactionEntity>,
        accounts: List<FinancialAccountEntity>,
        currencySymbol: String = "C$",
        userName: String = "Usuario FinanzIQ"
    ): File {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startStr = dateFormat.format(Date(startDateMillis))
        val endStr = dateFormat.format(Date(endDateMillis))

        // Documento PDF estándar A4 (595 x 842 puntos)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Configuración de pinceles y tipografías
        val paintTitle = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintSectionHeader = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintTableHeader = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintTableCell = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintTableCellBold = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintHeaderBg = Paint().apply {
            color = Color.rgb(2, 132, 199) // Sky 600
            style = Paint.Style.FILL
        }

        val paintRowBgEven = Paint().apply {
            color = Color.rgb(248, 250, 252) // Slate 50
            style = Paint.Style.FILL
        }

        val paintRowBgOdd = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val paintBorder = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val paintSuccess = Paint().apply {
            color = Color.rgb(22, 163, 74) // Green 600
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintError = Paint().apply {
            color = Color.rgb(220, 38, 38) // Red 600
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var currentY = 45f

        // --- ENCABEZADO DEL INFORME ---
        // Franja superior decorativa
        val paintTopBanner = Paint().apply {
            color = Color.rgb(3, 105, 161)
            style = Paint.Style.FILL
        }
        canvas.drawRect(36f, currentY, 559f, currentY + 6f, paintTopBanner)
        currentY += 26f

        canvas.drawText("FinanzIQ - Reporte Financiero", 36f, currentY, paintTitle)
        currentY += 16f
        canvas.drawText("Titular: $userName   |   Rango del Informe: $startStr al $endStr", 36f, currentY, paintSubtitle)
        currentY += 12f
        canvas.drawText("Fecha de Emisión: ${dateFormat.format(Date())}   |   Moneda Oficial: Córdobas ($currencySymbol)", 36f, currentY, paintSubtitle)
        currentY += 24f

        // --- CÁLCULO DE VALORES GENERALES ---
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        // --- TABLA 1: BALANCE GENERAL (INGRESOS Y GASTOS) ---
        canvas.drawText("1. TABLA DE BALANCE GENERAL (INGRESOS Y GASTOS)", 36f, currentY, paintSectionHeader)
        currentY += 10f

        val table1Left = 36f
        val table1Right = 559f
        val colWidthT1 = (table1Right - table1Left) / 4f

        // Encabezado Tabla 1
        canvas.drawRect(table1Left, currentY, table1Right, currentY + 22f, paintHeaderBg)
        canvas.drawText("Concepto Global", table1Left + 10f, currentY + 15f, paintTableHeader)
        canvas.drawText("Total Ingresos", table1Left + colWidthT1 + 10f, currentY + 15f, paintTableHeader)
        canvas.drawText("Total Gastos", table1Left + colWidthT1 * 2 + 10f, currentY + 15f, paintTableHeader)
        canvas.drawText("Balance Neto", table1Left + colWidthT1 * 3 + 10f, currentY + 15f, paintTableHeader)
        currentY += 22f

        // Fila de Datos Tabla 1
        canvas.drawRect(table1Left, currentY, table1Right, currentY + 24f, paintRowBgEven)
        canvas.drawRect(table1Left, currentY, table1Right, currentY + 24f, paintBorder)

        canvas.drawText("Período ($startStr - $endStr)", table1Left + 10f, currentY + 16f, paintTableCellBold)
        canvas.drawText("$currencySymbol ${String.format(Locale.US, "%,.2f", totalIncome)}", table1Left + colWidthT1 + 10f, currentY + 16f, paintSuccess)
        canvas.drawText("$currencySymbol ${String.format(Locale.US, "%,.2f", totalExpense)}", table1Left + colWidthT1 * 2 + 10f, currentY + 16f, paintError)

        val balancePaint = if (netBalance >= 0) paintSuccess else paintError
        canvas.drawText("$currencySymbol ${String.format(Locale.US, "%,.2f", netBalance)}", table1Left + colWidthT1 * 3 + 10f, currentY + 16f, balancePaint)

        currentY += 40f

        // --- TABLA 2: DESGLOSE POR BILLETERAS Y CUENTAS VINCULADAS ---
        canvas.drawText("2. TABLA DESGLOSADA POR BILLETERAS Y CUENTAS VINCULADAS", 36f, currentY, paintSectionHeader)
        currentY += 10f

        val table2Left = 36f
        val table2Right = 559f

        // Anchos de columnas para Tabla 2
        // Entidad / Herramienta (160) | Tipo (75) | Ingresos Período (95) | Gastos Período (95) | Saldo Actual (98)
        val wEntidad = 160f
        val wTipo = 75f
        val wIngresos = 95f
        val wGastos = 95f
        val wSaldo = 98f

        // Encabezado Tabla 2
        canvas.drawRect(table2Left, currentY, table2Right, currentY + 22f, paintHeaderBg)
        var colX = table2Left
        canvas.drawText("Entidad / Herramienta", colX + 8f, currentY + 15f, paintTableHeader)
        colX += wEntidad
        canvas.drawText("Tipo", colX + 8f, currentY + 15f, paintTableHeader)
        colX += wTipo
        canvas.drawText("Ingresos ($currencySymbol)", colX + 8f, currentY + 15f, paintTableHeader)
        colX += wIngresos
        canvas.drawText("Gastos ($currencySymbol)", colX + 8f, currentY + 15f, paintTableHeader)
        colX += wGastos
        canvas.drawText("Saldo Actual", colX + 8f, currentY + 15f, paintTableHeader)
        currentY += 22f

        // Filas para cada herramienta asociada al usuario
        var isEven = false
        for (account in accounts) {
            val bgPaint = if (isEven) paintRowBgEven else paintRowBgOdd
            isEven = !isEven

            canvas.drawRect(table2Left, currentY, table2Right, currentY + 22f, bgPaint)
            canvas.drawRect(table2Left, currentY, table2Right, currentY + 22f, paintBorder)

            // Movimientos específicos vinculados a esta cuenta en el periodo
            val accTxs = transactions.filter { tx ->
                val noteMatch = tx.note.contains(account.accountNumber) || tx.note.contains(account.bankName)
                val destMatch = tx.destination.contains(account.bankName, ignoreCase = true) || tx.destination.contains(account.accountNumber)
                noteMatch || destMatch
            }

            val accIncome = accTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val accExpense = accTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            colX = table2Left
            val toolLabel = "${account.bankName} (${account.accountNumber.takeLast(6)})"
            val shortLabel = if (toolLabel.length > 25) toolLabel.take(23) + ".." else toolLabel
            canvas.drawText(shortLabel, colX + 8f, currentY + 15f, paintTableCell)

            colX += wEntidad
            val typeLabel = if (account.toolType == "BILLETERA") "Billetera" else account.accountType
            canvas.drawText(typeLabel, colX + 8f, currentY + 15f, paintTableCell)

            colX += wTipo
            canvas.drawText(String.format(Locale.US, "%,.2f", accIncome), colX + 8f, currentY + 15f, paintTableCell)

            colX += wIngresos
            canvas.drawText(String.format(Locale.US, "%,.2f", accExpense), colX + 8f, currentY + 15f, paintTableCell)

            colX += wGastos
            canvas.drawText("$currencySymbol ${String.format(Locale.US, "%,.2f", account.balance)}", colX + 8f, currentY + 15f, paintTableCellBold)

            currentY += 22f
        }

        // Fila Totalizadora de Cuentas y Billeteras
        val totalAccountsBalance = accounts.filter { it.isActive }.sumOf { it.balance }
        canvas.drawRect(table2Left, currentY, table2Right, currentY + 24f, Color.rgb(241, 245, 249).let { c ->
            Paint().apply { color = c; style = Paint.Style.FILL }
        })
        canvas.drawRect(table2Left, currentY, table2Right, currentY + 24f, paintBorder)

        canvas.drawText("TOTAL CONSOLIDADO EN SALDOS", table2Left + 8f, currentY + 16f, paintTableCellBold)
        canvas.drawText(
            "$currencySymbol ${String.format(Locale.US, "%,.2f", totalAccountsBalance)}",
            table2Left + wEntidad + wTipo + wIngresos + wGastos + 8f,
            currentY + 16f,
            paintTableCellBold
        )

        currentY += 40f

        // Pie de Página
        val paintFooter = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Documento generado automáticamente por FinanzIQ. Válido como informe financiero personal.", 36f, 800f, paintFooter)

        document.finishPage(page)

        // Guardar archivo en almacenamiento en caché interno de la app
        val reportDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportDir, "Reporte_Financiero_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    /**
     * Lanza la acción de compartir / abrir el documento PDF generado utilizando FileProvider.
     */
    fun openOrSharePdf(context: Context, pdfFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte Financiero - FinanzIQ")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Descargar / Compartir Reporte PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
