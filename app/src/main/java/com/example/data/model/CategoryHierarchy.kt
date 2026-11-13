package com.example.data.model

import com.example.data.local.CategoryEntity
import com.example.util.AppLanguage
import java.util.Locale

data class SubcategoryItem(
    val id: String,
    val nameEs: String,
    val nameEn: String,
    val iconEmoji: String
)

data class CategoryItem(
    val id: String,
    val nameEs: String,
    val nameEn: String,
    val iconEmoji: String,
    val subcategories: List<SubcategoryItem> = emptyList()
)

enum class CategoryGroupType {
    FIXED_EXPENSE,
    VARIABLE_EXPENSE,
    DEBT_PAYMENT,
    ACTIVE_INCOME,
    PASSIVE_INCOME
}

data class CategoryGroup(
    val type: CategoryGroupType,
    val titleEs: String,
    val titleEn: String,
    val categories: List<CategoryItem>
)

data class PassiveIncomeCategory(
    val id: String,
    val titleEs: String,
    val titleEn: String,
    val iconEmoji: String,
    val explanationEs: String,
    val explanationEn: String
)

object CategoryDataHierarchy {
    val fixedExpenseGroup = CategoryGroup(
        type = CategoryGroupType.FIXED_EXPENSE,
        titleEs = "Gastos Fijos",
        titleEn = "Fixed Expenses",
        categories = listOf(
            // Categoría unificada de Vivienda (eliminada duplicación anterior "Vivienda/Hogar")
            CategoryItem(
                id = "vivienda",
                nameEs = "Vivienda",
                nameEn = "Housing",
                iconEmoji = "🏠"
            ),
            CategoryItem(
                id = "servicios_publicos",
                nameEs = "Servicios Públicos",
                nameEn = "Public Utilities",
                iconEmoji = "💡",
                subcategories = listOf(
                    SubcategoryItem("agua", "Agua", "Water", "💧"),
                    SubcategoryItem("luz", "Luz", "Electricity", "⚡"),
                    SubcategoryItem("internet_telefonia", "Internet y Telefonía", "Internet & Phone", "📶")
                )
            ),
            CategoryItem(
                id = "alimentacion_basica",
                nameEs = "Alimentación Básica",
                nameEn = "Basic Groceries",
                iconEmoji = "🛒"
            ),
            CategoryItem(
                id = "transporte",
                nameEs = "Transporte",
                nameEn = "Transportation",
                iconEmoji = "🚗",
                subcategories = listOf(
                    SubcategoryItem("gasolina", "Gasolina", "Gasoline", "⛽"),
                    SubcategoryItem("transporte_publico", "Transporte Público", "Public Transit", "🚌"),
                    SubcategoryItem("mantenimiento_auto", "Mantenimiento del auto", "Car Maintenance", "🔧"),
                    SubcategoryItem("peajes_estacionamiento", "Peajes y estacionamiento", "Tolls & Parking", "🅿️")
                )
            ),
            CategoryItem(
                id = "salud",
                nameEs = "Salud",
                nameEn = "Health",
                iconEmoji = "⚕️",
                subcategories = listOf(
                    SubcategoryItem("seguro_medico", "Seguro Médico", "Health Insurance", "🛡️"),
                    SubcategoryItem("medicamentos_receta", "Medicamentos con Receta", "Prescription Meds", "💊"),
                    SubcategoryItem("consultas_medicas", "Consultas Médicas y Dentales", "Medical & Dental Visits", "🩺")
                )
            ),
            CategoryItem(
                id = "educacion",
                nameEs = "Educación",
                nameEn = "Education",
                iconEmoji = "🎓",
                subcategories = listOf(
                    SubcategoryItem("matriculas", "Matrículas", "Tuition", "📝"),
                    SubcategoryItem("utiles_escolares", "Útiles Escolares", "School Supplies", "🎒")
                )
            )
        )
    )

    val variableExpenseGroup = CategoryGroup(
        type = CategoryGroupType.VARIABLE_EXPENSE,
        titleEs = "Gastos Variables (Deseos)",
        titleEn = "Variable Expenses (Wants)",
        categories = listOf(
            CategoryItem(
                id = "comida_fuera",
                nameEs = "Comida Fuera y Delivery",
                nameEn = "Dining Out & Delivery",
                iconEmoji = "🍔",
                subcategories = listOf(
                    SubcategoryItem("restaurantes", "Restaurantes", "Restaurants", "🍽️"),
                    SubcategoryItem("cafeterias", "Cafeterías", "Coffee Shops", "☕"),
                    SubcategoryItem("delivery_apps", "Apps de Delivery", "Delivery Apps", "🛵")
                )
            ),
            CategoryItem(
                id = "entretenimiento",
                nameEs = "Entretenimiento y Ocio",
                nameEn = "Entertainment & Leisure",
                iconEmoji = "🎮",
                subcategories = listOf(
                    SubcategoryItem("cine", "Cine", "Movies & Cinema", "🎬"),
                    SubcategoryItem("salidas_amigos", "Salidas con Amigos", "Outings with Friends", "👥"),
                    SubcategoryItem("libros", "Libros", "Books", "📚"),
                    SubcategoryItem("videojuegos", "Videojuegos y Pasatiempos", "Video Games & Hobbies", "🕹️")
                )
            ),
            CategoryItem(
                id = "suscripciones",
                nameEs = "Suscripciones Digitales",
                nameEn = "Digital Subscriptions",
                iconEmoji = "📱",
                subcategories = listOf(
                    SubcategoryItem("streaming_video", "Streaming de Video", "Video Streaming", "📺"),
                    SubcategoryItem("streaming_musica", "Streaming de Música", "Music Streaming", "🎵")
                )
            ),
            CategoryItem(
                id = "compras_personal",
                nameEs = "Compras y Cuidado Personal",
                nameEn = "Shopping & Self Care",
                iconEmoji = "🛍️",
                subcategories = listOf(
                    SubcategoryItem("ropa_calzado", "Ropa, Calzado y Accesorios", "Clothing & Shoes", "👗"),
                    SubcategoryItem("peluqueria_estetica", "Peluquería, Barbería y Estética", "Barber & Salon", "✂️"),
                    SubcategoryItem("gimnasio", "Gimnasio, Membresías Deportivas", "Gym & Sports Memberships", "🏋️")
                )
            ),
            CategoryItem(
                id = "regalos_donaciones",
                nameEs = "Regalos y Donaciones",
                nameEn = "Gifts & Donations",
                iconEmoji = "🎁",
                subcategories = listOf(
                    SubcategoryItem("regalos_cumple", "Regalos de Cumpleaños y fechas especiales", "Birthday & Holiday Gifts", "🎂"),
                    SubcategoryItem("donaciones_propinas", "Donaciones a Caridad o Propinas", "Charity Donations & Tips", "🤝")
                )
            )
        )
    )

    val debtPaymentGroup = CategoryGroup(
        type = CategoryGroupType.DEBT_PAYMENT,
        titleEs = "Pago de Deudas",
        titleEn = "Debt Payments",
        categories = listOf(
            CategoryItem(
                id = "creditos_prestamos",
                nameEs = "Créditos y Préstamos",
                nameEn = "Credits & Loans",
                iconEmoji = "💳",
                subcategories = listOf(
                    SubcategoryItem("tarjeta_credito", "Tarjeta de Crédito", "Credit Card", "💳"),
                    SubcategoryItem("prestamos_personales", "Préstamos Personales", "Personal Loans", "🤝"),
                    SubcategoryItem("prestamos_entidad", "Préstamos a Entidad Financiera", "Bank Loans", "🏦"),
                    SubcategoryItem("credito_auto", "Crédito Automotriz", "Auto Loan", "🚙"),
                    SubcategoryItem("prestamos_estudiantiles", "Préstamos Estudiantiles", "Student Loans", "🎓")
                )
            )
        )
    )

    val passiveIncomeCategories = listOf(
        PassiveIncomeCategory(
            id = "bienes_raices",
            titleEs = "Bienes Raíces",
            titleEn = "Real Estate",
            iconEmoji = "🏢",
            explanationEs = "Dinero generado por propiedades, como alquileres de casas, apartamentos, locales comerciales o terrenos.",
            explanationEn = "Money generated from properties, such as rentals of houses, apartments, commercial spaces, or land."
        ),
        PassiveIncomeCategory(
            id = "inversiones_financieras",
            titleEs = "Inversiones Financieras",
            titleEn = "Financial Investments",
            iconEmoji = "📈",
            explanationEs = "Ganancias obtenidas de dinero invertido, como dividendos de acciones, intereses bancarios o fondos de inversión.",
            explanationEn = "Earnings obtained from invested money, such as stock dividends, bank interest, or investment funds."
        ),
        PassiveIncomeCategory(
            id = "negocios_automatizados",
            titleEs = "Negocios Automatizados",
            titleEn = "Automated Businesses",
            iconEmoji = "🤖",
            explanationEs = "Ingresos producidos por sistemas, ventas digitales, regalías o negocios que operan sin requerir tu presencia constante.",
            explanationEn = "Income produced by digital systems, sales, royalties, or businesses operating without requiring your continuous presence."
        )
    )

    // Categoría unificada de Salario (eliminada duplicación anterior "Salario / Sueldo")
    val activeIncomeCategories = listOf(
        CategoryItem("salario", "Salario", "Salary", "💼"),
        CategoryItem("honorarios", "Honorarios Profesionales", "Professional Fees", "📄"),
        CategoryItem("ventas", "Ventas Directas", "Direct Sales", "🛍️"),
        CategoryItem("comisiones", "Comisiones", "Commissions", "🎯"),
        CategoryItem("otros_activos", "Otros Ingresos Activos", "Other Active Income", "💰")
    )

    val defaultDestinationAccounts = listOf(
        "Billetera Móvil (Billetera Digital)",
        "Cuenta de Ahorros BAC",
        "Cuenta Corriente Banpro",
        "Cuenta Banco Lafise",
        "Cuenta Banco Ficohsa",
        "Efectivo en Mano",
        "Interactive Brokers (Inversiones)",
        "Binance / Crypto Wallet",
        "PayPal / Wise"
    )

    /**
     * Retorna el nombre traducido del título de un movimiento o meta según el idioma activo del usuario.
     * Permite que títulos predeterminados o comunes (como 'Gasolina y Transporte', 'Pago de Quincena', etc.)
     * se muestren en inglés cuando el usuario tiene seleccionada dicha preferencia lingüística.
     */
    fun getTransactionTitleDisplayName(title: String, lang: AppLanguage): String {
        if (title.isBlank()) return title
        val trimmed = title.trim()

        if (lang == AppLanguage.SPANISH) {
            return when (trimmed.lowercase(Locale.ROOT)) {
                "gasoline & transportation", "gasoline and transportation" -> "Gasolina y Transporte"
                "bi-weekly paycheck", "biweekly paycheck" -> "Pago de Quincena"
                "supermarket la union" -> "Supermercado La Unión"
                "pharmacy & medicine", "pharmacy and medicine" -> "Farmacia y Medicinas"
                "movies & fast dinner", "movies and fast dinner" -> "Cine y Cena Rápida"
                "freelance project sale" -> "Venta de Proyecto Freelance"
                "electricity bill (enel)", "electricity bill enel" -> "Recibo de Luz Enel"
                "online finance course" -> "Curso de Finanzas Online"
                "water utility bill" -> "Recibo de Agua Potable"
                "fiber optic internet" -> "Internet de Fibra Óptica"
                "personal loan installment" -> "Cuota Préstamo Personal"
                "emergency fund" -> "Fondo de Emergencia"
                "vacation trip" -> "Viaje de Vacaciones"
                "test expense" -> "Gasto de prueba"
                "test income" -> "Ingreso de prueba"
                else -> trimmed
            }
        }

        // Traducción de títulos al inglés
        return when (trimmed.lowercase(Locale.ROOT)) {
            "gasolina y transporte" -> "Gasoline & Transportation"
            "pago de quincena" -> "Bi-weekly Paycheck"
            "supermercado la unión", "supermercado la union" -> "Supermarket La Union"
            "farmacia y medicinas" -> "Pharmacy & Medicine"
            "cine y cena rápida", "cine y cena rapida" -> "Movies & Fast Dinner"
            "venta de proyecto freelance" -> "Freelance Project Sale"
            "recibo de luz enel" -> "Electricity Bill (Enel)"
            "curso de finanzas online" -> "Online Finance Course"
            "recibo de agua potable" -> "Water Utility Bill"
            "internet de fibra óptica", "internet de fibra optica" -> "Fiber Optic Internet"
            "cuota préstamo personal", "cuota prestamo personal" -> "Personal Loan Installment"
            "fondo de emergencia" -> "Emergency Fund"
            "viaje de vacaciones" -> "Vacation Trip"
            "gasto de prueba" -> "Test Expense"
            "ingreso de prueba" -> "Test Income"
            else -> {
                // Si coincide directamente con una categoría, aplicar la traducción de la categoría
                getCategoryDisplayName(trimmed, lang)
            }
        }
    }

    /**
     * Retorna las categorías disponibles para un grupo específico de gastos o ingresos,
     * fusionando las categorías base predeterminadas con las creadas por el Administrador en Room.
     */
    fun getCategoriesForGroup(
        groupType: CategoryGroupType,
        customCategories: List<CategoryEntity> = emptyList()
    ): List<CategoryItem> {
        val baseList = when (groupType) {
            CategoryGroupType.FIXED_EXPENSE -> fixedExpenseGroup.categories
            CategoryGroupType.VARIABLE_EXPENSE -> variableExpenseGroup.categories
            CategoryGroupType.DEBT_PAYMENT -> debtPaymentGroup.categories
            CategoryGroupType.ACTIVE_INCOME -> activeIncomeCategories
            CategoryGroupType.PASSIVE_INCOME -> emptyList()
        }

        val matchingCustom = customCategories.filter { cat ->
            cat.colorHex == groupType.name ||
                (groupType == CategoryGroupType.FIXED_EXPENSE && cat.type == "EXPENSE" && (cat.colorHex.startsWith("#") || cat.colorHex.isBlank())) ||
                (groupType == CategoryGroupType.ACTIVE_INCOME && cat.type == "INCOME" && (cat.colorHex.startsWith("#") || cat.colorHex.isBlank()))
        }.map { entity ->
            CategoryItem(
                id = "custom_${entity.id}",
                nameEs = entity.name,
                nameEn = entity.name,
                iconEmoji = if (entity.iconName.isNotBlank() && entity.iconName.length <= 4) entity.iconName else "🏷️"
            )
        }

        return baseList + matchingCustom
    }

    /**
     * Retorna la lista de categorías para Ingreso Activo integrando las categorías del Administrador.
     */
    fun getActiveIncomeCategories(
        customCategories: List<CategoryEntity> = emptyList()
    ): List<CategoryItem> {
        return getCategoriesForGroup(CategoryGroupType.ACTIVE_INCOME, customCategories)
    }

    /**
     * Retorna la lista de categorías para Ingreso Pasivo integrando las creadas por el Administrador.
     */
    fun getPassiveIncomeCategories(
        customCategories: List<CategoryEntity> = emptyList()
    ): List<PassiveIncomeCategory> {
        val customPassive = customCategories.filter { it.colorHex == CategoryGroupType.PASSIVE_INCOME.name }.map { entity ->
            PassiveIncomeCategory(
                id = "custom_${entity.id}",
                titleEs = entity.name,
                titleEn = entity.name,
                iconEmoji = if (entity.iconName.isNotBlank() && entity.iconName.length <= 4) entity.iconName else "🏷️",
                explanationEs = "Ingreso pasivo registrado por administración.",
                explanationEn = "Passive income category configured by administration."
            )
        }
        return passiveIncomeCategories + customPassive
    }

    /**
     * Retorna todas las categorías de gasto combinadas (Fijos, Variables, Deuda y creadas por Admin).
     */
    fun getAllExpenseCategories(
        customCategories: List<CategoryEntity> = emptyList()
    ): List<CategoryItem> {
        return getCategoriesForGroup(CategoryGroupType.FIXED_EXPENSE, customCategories) +
            getCategoriesForGroup(CategoryGroupType.VARIABLE_EXPENSE, customCategories) +
            getCategoriesForGroup(CategoryGroupType.DEBT_PAYMENT, customCategories)
    }

    /**
     * Retorna el nombre traducido de una categoría o subcategoría según el idioma activo del usuario.
     * Si el idioma seleccionado es español, conserva el nombre en español.
     * Si es inglés, busca la traducción correspondiente en el catálogo jerárquico o en los términos equivalentes.
     */
    fun getCategoryDisplayName(name: String, lang: AppLanguage): String {
        if (name.isBlank()) return name
        val trimmed = name.trim()
        if (lang == AppLanguage.SPANISH) {
            // Si el nombre viene en inglés por algún registro previo, traducirlo a español
            val allCats = fixedExpenseGroup.categories + variableExpenseGroup.categories + debtPaymentGroup.categories + activeIncomeCategories
            allCats.find { it.nameEn.equals(trimmed, ignoreCase = true) }?.let { return it.nameEs }
            passiveIncomeCategories.find { it.titleEn.equals(trimmed, ignoreCase = true) }?.let { return it.titleEs }
            allCats.flatMap { it.subcategories }.find { it.nameEn.equals(trimmed, ignoreCase = true) }?.let { return it.nameEs }
            return trimmed
        }

        // Búsqueda en categorías principales
        val allCats = fixedExpenseGroup.categories + variableExpenseGroup.categories + debtPaymentGroup.categories + activeIncomeCategories
        allCats.find { it.nameEs.equals(trimmed, ignoreCase = true) }?.let { return it.nameEn }
        passiveIncomeCategories.find { it.titleEs.equals(trimmed, ignoreCase = true) }?.let { return it.titleEn }

        // Búsqueda en subcategorías
        allCats.flatMap { it.subcategories }.find { it.nameEs.equals(trimmed, ignoreCase = true) }?.let { return it.nameEn }

        // Equivalencias directas para categorías comunes y registros predeterminados
        return when (trimmed.lowercase(Locale.ROOT)) {
            "alimentación", "alimentacion", "alimentación básica", "alimentacion basica" -> "Basic Groceries"
            "transporte", "gasolina y transporte" -> "Transportation"
            "salario", "sueldo", "pago de quincena", "salarios" -> "Salary"
            "vivienda" -> "Housing"
            "servicios públicos", "servicios publicos", "servicios", "servicios básicos", "servicios basicos" -> "Public Utilities"
            "salud" -> "Health"
            "educación", "educacion" -> "Education"
            "comida fuera y delivery", "comida fuera", "comida" -> "Dining Out & Delivery"
            "entretenimiento y ocio", "entretenimiento", "ocio" -> "Entertainment & Leisure"
            "suscripciones digitales", "suscripciones" -> "Digital Subscriptions"
            "compras y cuidado personal", "compras", "ropa y calzado" -> "Shopping & Self Care"
            "regalos y donaciones" -> "Gifts & Donations"
            "créditos y préstamos", "creditos y prestamos", "créditos", "deudas", "pago de deudas" -> "Debt Payments"
            "bienes raíces", "bienes raices" -> "Real Estate"
            "inversiones financieras", "inversiones" -> "Financial Investments"
            "negocios automatizados", "negocio propio" -> "Automated Businesses"
            "honorarios profesionales", "honorarios" -> "Professional Fees"
            "ventas directas", "ventas" -> "Direct Sales"
            "comisiones" -> "Commissions"
            "otros ingresos activos", "otros ingresos" -> "Other Active Income"
            "otros gastos" -> "Other Expenses"
            "gasolina" -> "Gasoline"
            "agua" -> "Water"
            "luz", "electricidad" -> "Electricity"
            "internet", "internet y telefonía", "internet y telefonia" -> "Internet & Phone"
            "mantenimiento del auto" -> "Car Maintenance"
            "peajes y estacionamiento" -> "Tolls & Parking"
            "seguro médico", "seguro medico" -> "Health Insurance"
            "medicamentos con receta", "medicamentos" -> "Prescription Meds"
            "consultas médicas y dentales", "consultas medicas" -> "Medical & Dental Visits"
            "matrículas", "matriculas" -> "Tuition"
            "útiles escolares", "utiles escolares" -> "School Supplies"
            "restaurantes" -> "Restaurants"
            "cafeterías", "cafeterias" -> "Coffee Shops"
            "apps de delivery" -> "Delivery Apps"
            "cine" -> "Movies & Cinema"
            "salidas con amigos" -> "Outings with Friends"
            "libros" -> "Books"
            "videojuegos y pasatiempos" -> "Video Games & Hobbies"
            "streaming de video" -> "Video Streaming"
            "streaming de música", "streaming de musica" -> "Music Streaming"
            "ropa, calzado y accesorios" -> "Clothing & Shoes"
            "peluquería, barbería y estética", "peluqueria" -> "Barber & Salon"
            "gimnasio, membresías deportivas", "gimnasio" -> "Gym & Sports Memberships"
            "tarjeta de crédito", "tarjeta de credito" -> "Credit Card"
            "préstamos personales", "prestamos personales" -> "Personal Loans"
            "todas", "todas las categorías", "todas las categorias" -> "All Categories"
            "todos", "todos los tipos" -> "All Types"
            else -> trimmed
        }
    }
}
