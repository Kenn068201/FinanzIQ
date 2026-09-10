package com.example.util

enum class AppLanguage {
    SPANISH,
    ENGLISH
}

object Localization {
    fun t(key: String, lang: AppLanguage, vararg args: Any): String {
        val map = if (lang == AppLanguage.SPANISH) ES else EN
        val template = map[key] ?: (ES[key] ?: key)
        return try {
            if (args.isNotEmpty()) String.format(template, *args) else template
        } catch (e: Exception) {
            template
        }
    }

    private val ES = mapOf(
        // General & Nav
        "app_title" to "Finanzas Inteligentes",
        "app_subtitle" to "Control total de tus ingresos, gastos y presupuestos",
        "nav_dashboard" to "Inicio",
        "nav_transactions" to "Movimientos",
        "nav_budgets" to "Presupuestos",
        "nav_analytics" to "Análisis",
        "nav_savings" to "Ahorros",
        "nav_bills" to "Recibos",
        "nav_ai" to "Asistente IA",
        "welcome" to "BIENVENIDO",
        "logout" to "Cerrar Sesión",
        "cancel" to "Cancelar",
        "save" to "Guardar",
        "delete" to "Eliminar",
        "search" to "Buscar...",

        // Theme & Lang
        "light_mode" to "Modo Claro",
        "dark_mode" to "Modo Oscuro",
        "lang_es" to "Español",
        "lang_en" to "English",

        // Auth
        "login" to "Iniciar Sesión",
        "register" to "Registrarme",
        "create_account" to "Registro de Usuario",
        "email_or_user" to "Usuario o Correo Electrónico",
        "password" to "Contraseña",
        "confirm_password" to "Confirmar Contraseña",
        "first_names" to "Nombres",
        "last_names" to "Apellidos",
        "age" to "Edad",
        "phone" to "Teléfono",
        "preferred_currency" to "Moneda de Preferencia",
        "country_prefix" to "Prefijo Telefónico",
        "forgot_password" to "¿Olvidaste tu contraseña?",
        "no_account" to "¿No tienes una cuenta?",
        "have_account" to "¿Ya tienes una cuenta?",
        "lockout_warning" to "Has ingresado credenciales incorrectas 3 veces. Por seguridad, debes esperar 5 minutos para volver a intentar.",
        "time_remaining" to "Tiempo de espera restante: %s",
        "error_max_70_first_name" to "Has alcanzado el límite máximo de 70 caracteres para los nombres.",
        "error_max_70_last_name" to "Has alcanzado el límite máximo de 70 caracteres para los apellidos.",
        "error_email_already_registered" to "El correo electrónico ingresado ya está registrado en la base de datos.",
        "error_phone_already_registered" to "El número de teléfono ya ha sido registrado por otro usuario.",

        // Income & Expense Form
        "new_movement" to "Registrar Movimiento",
        "income" to "Ingreso",
        "expense" to "Gasto",
        "active_income" to "Ingreso Activo",
        "passive_income" to "Ingreso Pasivo",
        "concept" to "Concepto / Descripción",
        "amount" to "Monto",
        "frequency" to "Frecuencia de Cobro",
        "payout_date" to "Fecha de Cobro",
        "destination_account" to "Billetera, cuenta bancaria o broker de destino",
        "category" to "Categoría",
        "subcategory" to "Subcategoría",
        "notes" to "Notas adicionales (opcional)",

        // Frequencies
        "freq_monthly" to "Mensual",
        "freq_quarterly" to "Trimestral",
        "freq_biannual" to "Semestral",
        "freq_annual" to "Anual",
        "freq_sporadic" to "Esporádico",

        // Passive Income categories & explanations
        "cat_real_estate" to "Bienes Raíces",
        "desc_real_estate" to "Dinero generado por propiedades, como alquileres de casas, apartamentos, locales comerciales o terrenos.",
        "cat_financial_inv" to "Inversiones Financieras",
        "desc_financial_inv" to "Ganancias obtenidas de dinero invertido, como dividendos de acciones, intereses bancarios o fondos de inversión.",
        "cat_automated_biz" to "Negocios Automatizados",
        "desc_automated_biz" to "Ingresos producidos por sistemas, ventas digitales, regalías o negocios que operan sin requerir tu presencia constante.",

        // Groups
        "group_fixed_expenses" to "Gastos Fijos",
        "group_variable_expenses" to "Gastos Variables (Deseos)",
        "group_debt_payments" to "Pago de Deudas",

        // Budget & AI
        "wants_budget_title" to "Presupuesto Mensual: Deseos",
        "wants_budget_subtitle" to "Fondo global para tus gustos y gastos variables",
        "ai_wants_message" to "Tienes %s disponible este mes para gastar en lo que quieras.",
        "fixed_auto_calculated" to "Total calculado automáticamente según los gastos fijos del mes anterior: %s",
        "fixed_auto_subtitle" to "Los gastos fijos no requieren un límite manual; se proyectan con base en tu historial real.",
        "spent" to "Gastado",
        "available" to "Disponible",
        "total_budget" to "Presupuesto Total",

        // Analytics
        "analytics_title" to "Análisis y Desglose de Fondos",
        "global_fund_remaining" to "Fondo Global Restante",
        "nested_chart_title" to "Distribución por Categorías y Subcategorías"
    )

    private val EN = mapOf(
        // General & Nav
        "app_title" to "Smart Finances",
        "app_subtitle" to "Complete control of your income, expenses, and budgets",
        "nav_dashboard" to "Home",
        "nav_transactions" to "Transactions",
        "nav_budgets" to "Budgets",
        "nav_analytics" to "Analytics",
        "nav_savings" to "Savings",
        "nav_bills" to "Bills",
        "nav_ai" to "AI Assistant",
        "welcome" to "WELCOME",
        "logout" to "Sign Out",
        "cancel" to "Cancel",
        "save" to "Save",
        "delete" to "Delete",
        "search" to "Search...",

        // Theme & Lang
        "light_mode" to "Light Mode",
        "dark_mode" to "Dark Mode",
        "lang_es" to "Español",
        "lang_en" to "English",

        // Auth
        "login" to "Sign In",
        "register" to "Sign Up",
        "create_account" to "User Registration",
        "email_or_user" to "Username or Email Address",
        "password" to "Password",
        "confirm_password" to "Confirm Password",
        "first_names" to "First Names",
        "last_names" to "Last Names",
        "age" to "Age",
        "phone" to "Phone Number",
        "preferred_currency" to "Preferred Currency",
        "country_prefix" to "Country Calling Code",
        "forgot_password" to "Forgot your password?",
        "no_account" to "Don't have an account?",
        "have_account" to "Already have an account?",
        "lockout_warning" to "You have entered incorrect credentials 3 times. For security, please wait 5 minutes before trying again.",
        "time_remaining" to "Remaining wait time: %s",
        "error_max_70_first_name" to "You have reached the maximum limit of 70 characters for first names.",
        "error_max_70_last_name" to "You have reached the maximum limit of 70 characters for last names.",
        "error_email_already_registered" to "The email entered is already registered in the database.",
        "error_phone_already_registered" to "The phone number is already registered by another user.",

        // Income & Expense Form
        "new_movement" to "New Transaction",
        "income" to "Income",
        "expense" to "Expense",
        "active_income" to "Active Income",
        "passive_income" to "Passive Income",
        "concept" to "Concept / Description",
        "amount" to "Amount",
        "frequency" to "Collection Frequency",
        "payout_date" to "Payout Date",
        "destination_account" to "Destination wallet, bank account, or broker",
        "category" to "Category",
        "subcategory" to "Subcategory",
        "notes" to "Additional notes (optional)",

        // Frequencies
        "freq_monthly" to "Monthly",
        "freq_quarterly" to "Quarterly",
        "freq_biannual" to "Semi-annually",
        "freq_annual" to "Annually",
        "freq_sporadic" to "Sporadic",

        // Passive Income categories & explanations
        "cat_real_estate" to "Real Estate",
        "desc_real_estate" to "Money generated from properties, such as rentals of homes, apartments, commercial spaces, or land.",
        "cat_financial_inv" to "Financial Investments",
        "desc_financial_inv" to "Earnings obtained from invested money, such as stock dividends, bank interest, or investment funds.",
        "cat_automated_biz" to "Automated Businesses",
        "desc_automated_biz" to "Income produced by digital systems, sales, royalties, or businesses that run without requiring your continuous presence.",

        // Groups
        "group_fixed_expenses" to "Fixed Expenses",
        "group_variable_expenses" to "Variable Expenses (Wants)",
        "group_debt_payments" to "Debt Payments",

        // Budget & AI
        "wants_budget_title" to "Monthly Budget: Wants",
        "wants_budget_subtitle" to "Global fund for your personal treats and variable spending",
        "ai_wants_message" to "You have %s available this month to spend on whatever you want.",
        "fixed_auto_calculated" to "Total automatically calculated from last month's fixed expenses: %s",
        "fixed_auto_subtitle" to "Fixed expenses do not require a manual limit; they are projected based on your actual history.",
        "spent" to "Spent",
        "available" to "Available",
        "total_budget" to "Total Budget",

        // Analytics
        "analytics_title" to "Analytics & Fund Breakdown",
        "global_fund_remaining" to "Remaining Global Fund",
        "nested_chart_title" to "Breakdown by Categories and Subcategories"
    )
}
