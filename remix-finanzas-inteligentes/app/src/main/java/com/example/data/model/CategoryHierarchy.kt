package com.example.data.model

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
            CategoryItem(
                id = "vivienda",
                nameEs = "Vivienda/Hogar",
                nameEn = "Housing/Home",
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

    val activeIncomeCategories = listOf(
        CategoryItem("salario", "Salario / Sueldo", "Salary / Wages", "💼"),
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
}
