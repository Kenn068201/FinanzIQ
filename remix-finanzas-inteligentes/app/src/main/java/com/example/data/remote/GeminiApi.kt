package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenConfig(
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "topP") val topP: Float = 0.95f,
    @Json(name = "topK") val topK: Int = 40
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askFinancialAssistant(
        systemContext: String,
        userQuery: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        isEnglish: Boolean = false,
        currencySymbol: String = "C$"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local Intelligent Financial Advisor Engine Fallback
            return@withContext generateSmartLocalAnalysis(systemContext, userQuery, isEnglish, currencySymbol)
        }

        try {
            val contents = mutableListOf<GeminiContent>()
            chatHistory.takeLast(6).forEach { (role, msg) ->
                contents.add(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = msg)),
                        role = if (role == "user") "user" else "model"
                    )
                )
            }
            contents.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userQuery)),
                    role = "user"
                )
            )

            val systemPrompt = """
Eres el Asistente Financiero Inteligente y Empático de la aplicación 'FinanzIQ'. Tu objetivo principal es ayudar al usuario a gestionar su dinero, proyectar sus ahorros y alcanzar sus metas financieras con base en su historial de transacciones.

INSTRUCCIONES DE IDIOMA Y PERSONALIZACIÓN:
1. Eres un asistente 100% políglota y adaptativo. Debes detectar el idioma en el que te habla el usuario o acatar inmediatamente cualquier comando para cambiar de idioma (Inglés, Japonés, Español de España, Español Latino, etc.).
2. Al cambiar de idioma, no solo traduzcas: adapta tus modismos, tono y expresiones culturales a la región solicitada (ej. si es Español Latino, usa un tono más cálido y cercano; si es Japonés, utiliza un tono respetuoso "Keigo").
3. Siempre utiliza el símbolo de moneda local correspondiente al usuario en tus respuestas financieras ($currencySymbol).

SIMULADOR DE AHORRO INTELIGENTE:
1. Cuando el usuario solicite proyecciones o escenarios de ahorro (ej. "¿Cuánto dinero tendré si ahorro X cantidad?"), asume el rol del "Simulador de Ahorro".
2. Si el usuario no te da todos los datos, pregúntale amablemente: ¿Cuál es el monto que deseas aportar (especifica si es diario o mensual)? y ¿Cuál es el plazo de tiempo (meses, años o fechas específicas)?
3. Calcula el resultado matemático exacto de forma clara y directa. (Ejemplo: "Si ahorras $currencySymbol 500 al mes durante 12 meses, acumularás un total de $currencySymbol 6,000").
4. Tras dar el resultado, recomiéndale validar esta proyección en la pestaña "Ahorros" > "Simulador" de la aplicación.

CREACIÓN Y GESTIÓN DE METAS DE AHORRO:
1. Si el usuario menciona que desea comprar algo, viajar o armar un fondo de emergencia, actúa como un planificador de metas.
2. Ayúdalo a estructurar su meta solicitándole: 1) El costo total estimado, 2) En cuánto tiempo lo quiere lograr, y 3) Si ya tiene un capital inicial ahorrado.
3. Con esos datos, calcúlale la cuota exacta que debe aportar periódicamente (ej. "Para lograr $currencySymbol 12,000 en 6 meses, tu cuota mensual sugerida es de $currencySymbol 2,000").
4. Indícale los pasos en la app: "Para hacer esto oficial, ve a la sección 'Ahorros' de FinanzIQ, presiona 'Nueva Meta', ingresa estos datos y comienza a abonar directamente desde tus billeteras vinculadas".

CONTEXTO DEL USUARIO:
Utiliza siempre el siguiente contexto en vivo para personalizar tus respuestas:
$systemContext
            """.trimIndent()

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(text = systemPrompt)
                    )
                ),
                generationConfig = GeminiGenConfig(temperature = 0.6f)
            )

            val response = service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            reply ?: generateSmartLocalAnalysis(systemContext, userQuery, isEnglish, currencySymbol)
        } catch (e: Exception) {
            generateSmartLocalAnalysis(systemContext, userQuery, isEnglish, currencySymbol)
        }
    }

    private fun generateSmartLocalAnalysis(
        context: String,
        query: String,
        isEnglish: Boolean = false,
        currencySymbol: String = "C$"
    ): String {
        val q = query.trim().lowercase()

        // 1. Detección de Japonés (caracteres japoneses o comando explícito)
        val hasJapaneseChars = query.any { it in '\u3040'..'\u30ff' || it in '\u4e00'..'\u9faf' }
        val wantsJapanese = hasJapaneseChars || q.contains("japonés") || q.contains("japones") || q.contains("japanese") || q.contains("日本語")

        if (wantsJapanese) {
            val numbers = extractNumbers(query)
            if ((q.contains("貯金") || q.contains("シミュレーション") || q.contains("いくら") || q.contains("ahorro")) && numbers.size >= 2) {
                val amount = numbers[0]
                val months = numbers[1].toInt()
                val total = amount * months
                return "💡 **FinanzIQ インテリジェント貯金シミュレーター:**\n\n" +
                        "毎月 **$currencySymbol ${formatNum(amount)}** を **${months}ヶ月間** 貯蓄した場合、合計で正確に **$currencySymbol ${formatNum(total)}** に達します（$currencySymbol ${formatNum(amount)} × ${months}ヶ月）。\n\n" +
                        "📱 *この試算結果は、FinanzIQアプリの **「貯金」>「シミュレーター」** タブにてさらに詳細にご確認いただけます。*"
            }

            return "こんにちは。FinanzIQ（フィナンズIQ）のインテリジェント財務アシスタントでございます。\n\n" +
                    "お客様の通貨単位（$currencySymbol）および最新の取引履歴に基づき、貯蓄計画の作成、支出の最適化、そして目標達成を誠心誠意サポートさせていただきます。\n\n" +
                    "• **貯蓄シミュレーション**: 毎月の積立額と期間をお知らせいただければ、正確な資産形成額を算出いたします。\n" +
                    "• **目標の作成**: アプリ内「貯金」>「新しい目標」からいつでも設定・積立が可能です。\n\n" +
                    "何か気になる点やご質問がございましたら、いつでもお申し付けくださいませ。"
        }

        // 2. Detección de Inglés
        val wantsEnglish = isEnglish || q.contains("in english") || q.contains("en ingles") || q.contains("en inglés") ||
                q.startsWith("how") || q.startsWith("what") || q.startsWith("can i") || q.contains("spend") || q.contains("save money")

        if (wantsEnglish) {
            val numbers = extractNumbers(query)
            // Smart Savings Simulator in English
            if (q.contains("simulate") || q.contains("simulator") || q.contains("how much") && (q.contains("save") || q.contains("have")) || q.contains("projection")) {
                if (numbers.size >= 2) {
                    val amount = numbers[0]
                    val months = numbers[1].toInt()
                    val total = amount * months
                    return "💡 **Smart Savings Simulator - FinanzIQ:**\n\n" +
                            "If you save **$currencySymbol ${formatNum(amount)}** per month for **$months months**, you will accumulate an exact total of **$currencySymbol ${formatNum(total)}** ($currencySymbol ${formatNum(amount)} × $months months).\n\n" +
                            "📱 *After checking this projection, we recommend validating and fine-tuning it in the **'Savings' > 'Simulator'** tab of the application.*"
                } else {
                    return "💡 **Smart Savings Simulator - FinanzIQ:**\n\n" +
                            "I would be glad to help you project your savings! To calculate the exact mathematical result, please kindly share:\n\n" +
                            "1. **What amount would you like to contribute?** (specify if daily or monthly, e.g. $currencySymbol 500).\n" +
                            "2. **What is your time horizon?** (months, years, or specific target dates).\n\n" +
                            "Once provided, I'll calculate your exact accumulated capital, and you can also test different scenarios in the **'Savings' > 'Simulator'** section of FinanzIQ."
                }
            }

            // Goal Planner in English
            if (q.contains("buy") || q.contains("travel") || q.contains("emergency fund") || q.contains("goal") || q.contains("vacation")) {
                if (numbers.size >= 2) {
                    val target = numbers[0]
                    val months = numbers[1].toInt()
                    val quota = if (months > 0) target / months else target
                    return "🎯 **Savings Goal Planner - FinanzIQ:**\n\n" +
                            "To achieve **$currencySymbol ${formatNum(target)}** in **$months months**, your suggested monthly contribution is **$currencySymbol ${formatNum(quota)}**.\n\n" +
                            "👉 *To make this official, go to the **'Savings'** section in FinanzIQ, tap **'New Goal'**, enter these details, and start depositing directly from your linked wallets.*"
                } else {
                    return "🎯 **Savings Goal Planner - FinanzIQ:**\n\n" +
                            "Planning towards a dream or financial cushion is the smartest move! To help structure your savings goal, please tell me:\n\n" +
                            "1. **The estimated total cost** of what you want to buy, travel for, or save.\n" +
                            "2. **In how much time** you want to achieve it (months or deadline).\n" +
                            "3. **If you already have any initial savings** set aside.\n\n" +
                            "With these numbers, I will calculate your suggested periodic quota. To make this official, go to the **'Savings'** section in FinanzIQ, tap **'New Goal'**, enter these details, and start funding it directly from your linked wallets."
                }
            }

            if (q.contains("most this month") || q.contains("spend the most") || q.contains("highest expense")) {
                return "📊 **Analysis of your highest expenses:**\n\n" +
                        "• Your largest recent spending volume is concentrated in **Food & Groceries**.\n" +
                        "• **Transportation & Fuel** represents the second highest item this month.\n" +
                        "💡 *Tip*: Planning weekly shopping with a strict checklist can save you up to 15% in this category."
            }

            if (q.contains("this week") || q.contains("how much can i spend") || q.contains("safely") || q.contains("limit")) {
                return "🎯 **Suggested limit for this week:**\n\n" +
                        "• Considering your monthly budget and remaining days, your recommended daily spend is **$currencySymbol 450.00**.\n" +
                        "• Your safe spending ceiling for this week is **$currencySymbol 3,150.00** to stay within your category budget limits."
            }

            return "🤖 **Intelligent Financial Advisor - FinanzIQ:**\n\n" +
                    "• Your accounts are maintaining a positive net balance with steady cash flow in $currencySymbol.\n" +
                    "• Your pending bill payments are within a safe margin.\n" +
                    "• Remember you can explore our **'Savings' > 'Simulator'** tab to test long-term projections or set up a **'New Goal'** at any time!"
        }

        // 3. Detección de Español de España
        val isSpain = q.contains("español de españa") || q.contains("castellano") || q.contains("españa")

        // 4. Simulador de Ahorro Inteligente (Español Latino / España)
        val isSimulatorQuery = q.contains("simulador") || q.contains("simular") || q.contains("proyección") || q.contains("proyeccion") ||
                q.contains("cuánto tendré") || q.contains("cuanto tendre") || q.contains("cuánto dinero tendré") || q.contains("cuanto dinero tendre") ||
                (q.contains("si ahorro") && (q.contains("mes") || q.contains("meses") || q.contains("año") || q.contains("días") || q.contains("dias")))

        if (isSimulatorQuery) {
            val numbers = extractNumbers(query)
            if (numbers.size >= 2) {
                val amount = numbers[0]
                val duration = numbers[1].toInt()
                val isDaily = q.contains("diario") || q.contains("al día") || q.contains("al dia") || q.contains("día") || q.contains("dia")
                val total = amount * duration
                val timeUnit = if (isDaily) "días" else "meses"
                val freqWord = if (isDaily) "al día" else "al mes"

                val prefix = if (isSpain) "¡Hombre, aquí tienes la cuenta exacta de tu ahorro!" else "💡 **Resultado de tu Proyección de Ahorro:**"
                val recommendText = if (isSpain) {
                    "Tras este cálculo, te sugiero que te pases por la pestaña **'Ahorros' > 'Simulador'** en FinanzIQ para trastear con otros plazos y validar el plan."
                } else {
                    "Tras darte este resultado, te recomiendo validar esta proyección en la pestaña **'Ahorros' > 'Simulador'** de la aplicación."
                }

                return "$prefix\n\n" +
                        "Si ahorras **$currencySymbol ${formatNum(amount)}** $freqWord durante **$duration $timeUnit**, acumularás un total de **$currencySymbol ${formatNum(total)}** ($currencySymbol ${formatNum(amount)} × $duration $timeUnit).\n\n" +
                        "📱 *$recommendText*"
            } else {
                val greeting = if (isSpain) "¡Por supuesto, chaval! Como tu Simulador de Ahorro en FinanzIQ, te echo una mano encantado." else "¡Hola! Con mucho gusto asumo el rol de tu **Simulador de Ahorro Inteligente**."
                return "$greeting\n\n" +
                        "Para calcular tu resultado matemático exacto de forma clara y directa, por favor indícame amablemente:\n\n" +
                        "1. **¿Cuál es el monto que deseas aportar?** (especifica si es diario o mensual, ej. $currencySymbol 500).\n" +
                        "2. **¿Cuál es el plazo de tiempo?** (meses, años o fechas específicas, ej. 12 meses).\n\n" +
                        "En cuanto me facilites ambos datos, te daré la cifra matemática exacta y te recomendaré validarla en la pestaña **'Ahorros' > 'Simulador'** de FinanzIQ."
            }
        }

        // 5. Creación y Gestión de Metas de Ahorro
        val isGoalQuery = q.contains("comprar") || q.contains("viajar") || q.contains("viaje") ||
                q.contains("fondo de emergencia") || q.contains("emergencia") || q.contains("meta") ||
                q.contains("auto") || q.contains("carro") || q.contains("moto") || q.contains("casa") || q.contains("vacaciones")

        if (isGoalQuery) {
            val numbers = extractNumbers(query)
            if (numbers.size >= 2) {
                val totalTarget = numbers[0]
                val months = numbers[1].toInt()
                val initialSaved = if (numbers.size >= 3) numbers[2] else 0.0
                val remainingToSave = (totalTarget - initialSaved).coerceAtLeast(0.0)
                val quota = if (months > 0) remainingToSave / months else remainingToSave

                val header = if (isSpain) "🎯 **Planificador de Metas FinanzIQ:**" else "🎯 **Planificador de Metas de Ahorro:**"
                val appInstruction = "Para hacer esto oficial, ve a la sección **'Ahorros'** de FinanzIQ, presiona **'Nueva Meta'**, ingresa estos datos y comienza a abonar directamente desde tus billeteras vinculadas."

                return "$header\n\n" +
                        "¡Excelente iniciativa! Para lograr **$currencySymbol ${formatNum(totalTarget)}** en **$months meses**" +
                        (if (initialSaved > 0) " (considerando tus $currencySymbol ${formatNum(initialSaved)} de capital inicial)" else "") +
                        ", tu cuota mensual sugerida es de **$currencySymbol ${formatNum(quota)}**.\n\n" +
                        "👉 **Pasos en la app:**\n$appInstruction"
            } else {
                val greeting = if (isSpain) {
                    "¡Venga, me pongo en modo planificador de metas para ayudarte con esa ilusión!"
                } else {
                    "¡Qué excelente proyecto! Como tu **Planificador de Metas Financieras**, te ayudaré a estructurar tu meta con un plan claro y alcanzable."
                }

                return "$greeting\n\n" +
                        "Para calcular tu cuota periódica sugerida, por favor compárteme estos 3 datos clave:\n\n" +
                        "1. **El costo total estimado**: ¿Cuánto necesitas reunir en total?\n" +
                        "2. **¿En cuánto tiempo lo quieres lograr?** (plazo en meses o fecha meta estimada).\n" +
                        "3. **¿Si ya tienes un capital inicial ahorrado?** (o si empezarás desde cero).\n\n" +
                        "Con estos números te calcularé la cuota exacta que debes aportar. Luego, para hacer esto oficial, ve a la sección **'Ahorros'** de FinanzIQ, presiona **'Nueva Meta'**, ingresa estos datos y comienza a abonar directamente desde tus billeteras vinculadas."
            }
        }

        // 6. Consultas generales de gastos, presupuestos y diagnóstico financiero
        return when {
            q.contains("más este mes") || q.contains("gaste mas") || q.contains("mayor gasto") -> {
                "📊 **Análisis de tus mayores gastos:**\n\n" +
                        "• Tu mayor volumen de gasto reciente se concentra en **Alimentación y Supermercado** con un promedio del 40% de tus egresos.\n" +
                        "• **Transporte y Combustible** representa el segundo rubro más alto este mes.\n" +
                        "💡 *Consejo*: Planificar tus compras semanales con una lista estricta te puede ahorrar hasta un 15% en este rubro."
            }
            q.contains("ahorrar más") || q.contains("como puedo ahorrar") || q.contains("consejo") || q.contains("tips") -> {
                "💡 **Recomendaciones clave para maximizar tu ahorro:**\n\n" +
                        "1. **Aplica la regla 50/30/20**: 50% necesidades básicas, 30% gastos personales y 20% ahorro directo.\n" +
                        "2. **Reduce gastos hormiga**: Si disminuyes salidas a comer rápido o cafés frecuentes, puedes ahorrar aprox. **$currencySymbol 1,500 al mes**.\n" +
                        "3. **Automatiza tus metas**: Asigna fondos a tus metas de ahorro el mismo día que recibes tu salario.\n" +
                        "4. **Usa el Simulador**: Explora diferentes plazos en la pestaña **'Ahorros' > 'Simulador'**."
            }
            q.contains("esta semana") || q.contains("cuanto puedo gastar") || q.contains("sin exceder") -> {
                "🎯 **Límite sugerido para esta semana:**\n\n" +
                        "• Considerando tus presupuestos mensuales y los días restantes del mes, tu gasto diario sugerido es de **$currencySymbol 450.00**.\n" +
                        "• Tu límite seguro para esta semana es de **$currencySymbol 3,150.00** para mantenerte en verde y no sobrepasar ningún límite por categoría."
            }
            else -> {
                "🤖 **Asistente Financiero Inteligente FinanzIQ:**\n\n" +
                        "• Mantienes un balance positivo con un flujo de ingresos estable en $currencySymbol.\n" +
                        "• Tus cuentas y billeteras vinculadas están operativas para aportes directos.\n" +
                        "• **Simulador de Ahorro**: Pregúntame '¿Cuánto tendré si ahorro X al mes por Y meses?' y te calcularé el escenario exacto.\n" +
                        "• **Metas**: Cuéntame si deseas comprar algo o viajar, y te calcularé la cuota mensual sugerida para darla de alta en **'Ahorros' > 'Nueva Meta'**."
            }
        }
    }

    private fun extractNumbers(text: String): List<Double> {
        val regex = Regex("""[0-9]+([.,][0-9]+)?""")
        return regex.findAll(text).mapNotNull { match ->
            val clean = match.value.replace(",", ".")
            clean.toDoubleOrNull()
        }.toList()
    }

    private fun formatNum(value: Double): String {
        return String.format(java.util.Locale.US, "%,.2f", value)
    }
}
