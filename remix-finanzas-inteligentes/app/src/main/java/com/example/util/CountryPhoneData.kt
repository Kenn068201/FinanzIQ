package com.example.util

data class CountryPhoneConfig(
    val code: String, // e.g. "NI"
    val nameEs: String,
    val nameEn: String,
    val flag: String,
    val dialPrefix: String, // e.g. "+505"
    val minDigits: Int,
    val maxDigits: Int,
    val allowedStartDigits: List<Char> = emptyList(),
    val placeholder: String,
    val formatHint: String
)

data class CurrencyOption(
    val code: String, // "NIO", "USD", "MXN", etc.
    val symbol: String, // "C$", "$", "R$", "Q", etc.
    val nameEs: String,
    val nameEn: String,
    val flag: String
)

object CountryPhoneData {
    val americanCountries = listOf(
        CountryPhoneConfig(
            code = "NI",
            nameEs = "Nicaragua",
            nameEn = "Nicaragua",
            flag = "🇳🇮",
            dialPrefix = "+505",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('5', '7', '8'),
            placeholder = "88997766",
            formatHint = "8 dígitos, inicia con 5, 7 u 8"
        ),
        CountryPhoneConfig(
            code = "US",
            nameEs = "Estados Unidos",
            nameEn = "United States",
            flag = "🇺🇸",
            dialPrefix = "+1",
            minDigits = 10,
            maxDigits = 10,
            placeholder = "2025550143",
            formatHint = "10 dígitos"
        ),
        CountryPhoneConfig(
            code = "CA",
            nameEs = "Canadá",
            nameEn = "Canada",
            flag = "🇨🇦",
            dialPrefix = "+1",
            minDigits = 10,
            maxDigits = 10,
            placeholder = "4165550198",
            formatHint = "10 dígitos"
        ),
        CountryPhoneConfig(
            code = "MX",
            nameEs = "México",
            nameEn = "Mexico",
            flag = "🇲🇽",
            dialPrefix = "+52",
            minDigits = 10,
            maxDigits = 10,
            placeholder = "5512345678",
            formatHint = "10 dígitos"
        ),
        CountryPhoneConfig(
            code = "GT",
            nameEs = "Guatemala",
            nameEn = "Guatemala",
            flag = "🇬🇹",
            dialPrefix = "+502",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('2', '3', '4', '5'),
            placeholder = "51234567",
            formatHint = "8 dígitos, inicia con 2, 3, 4 o 5"
        ),
        CountryPhoneConfig(
            code = "SV",
            nameEs = "El Salvador",
            nameEn = "El Salvador",
            flag = "🇸🇻",
            dialPrefix = "+503",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('2', '6', '7'),
            placeholder = "71234567",
            formatHint = "8 dígitos, inicia con 2, 6 o 7"
        ),
        CountryPhoneConfig(
            code = "HN",
            nameEs = "Honduras",
            nameEn = "Honduras",
            flag = "🇭🇳",
            dialPrefix = "+504",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('2', '3', '7', '8', '9'),
            placeholder = "91234567",
            formatHint = "8 dígitos, inicia con 2, 3, 7, 8 o 9"
        ),
        CountryPhoneConfig(
            code = "CR",
            nameEs = "Costa Rica",
            nameEn = "Costa Rica",
            flag = "🇨🇷",
            dialPrefix = "+506",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('2', '4', '6', '7', '8'),
            placeholder = "81234567",
            formatHint = "8 dígitos, inicia con 2, 4, 6, 7 u 8"
        ),
        CountryPhoneConfig(
            code = "PA",
            nameEs = "Panamá",
            nameEn = "Panama",
            flag = "🇵🇦",
            dialPrefix = "+507",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('2', '3', '6', '7', '8'),
            placeholder = "61234567",
            formatHint = "8 dígitos"
        ),
        CountryPhoneConfig(
            code = "CO",
            nameEs = "Colombia",
            nameEn = "Colombia",
            flag = "🇨🇴",
            dialPrefix = "+57",
            minDigits = 10,
            maxDigits = 10,
            allowedStartDigits = listOf('3'),
            placeholder = "3001234567",
            formatHint = "10 dígitos, inicia con 3"
        ),
        CountryPhoneConfig(
            code = "VE",
            nameEs = "Venezuela",
            nameEn = "Venezuela",
            flag = "🇻🇪",
            dialPrefix = "+58",
            minDigits = 10,
            maxDigits = 10,
            allowedStartDigits = listOf('4', '2'),
            placeholder = "4121234567",
            formatHint = "10 dígitos"
        ),
        CountryPhoneConfig(
            code = "EC",
            nameEs = "Ecuador",
            nameEn = "Ecuador",
            flag = "🇪🇨",
            dialPrefix = "+593",
            minDigits = 9,
            maxDigits = 9,
            allowedStartDigits = listOf('9'),
            placeholder = "987654321",
            formatHint = "9 dígitos, inicia con 9"
        ),
        CountryPhoneConfig(
            code = "PE",
            nameEs = "Perú",
            nameEn = "Peru",
            flag = "🇵🇪",
            dialPrefix = "+51",
            minDigits = 9,
            maxDigits = 9,
            allowedStartDigits = listOf('9'),
            placeholder = "987654321",
            formatHint = "9 dígitos, inicia con 9"
        ),
        CountryPhoneConfig(
            code = "BR",
            nameEs = "Brasil",
            nameEn = "Brazil",
            flag = "🇧🇷",
            dialPrefix = "+55",
            minDigits = 10,
            maxDigits = 11,
            placeholder = "11987654321",
            formatHint = "10-11 dígitos"
        ),
        CountryPhoneConfig(
            code = "BO",
            nameEs = "Bolivia",
            nameEn = "Bolivia",
            flag = "🇧🇴",
            dialPrefix = "+591",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('6', '7'),
            placeholder = "71234567",
            formatHint = "8 dígitos, inicia con 6 o 7"
        ),
        CountryPhoneConfig(
            code = "CL",
            nameEs = "Chile",
            nameEn = "Chile",
            flag = "🇨🇱",
            dialPrefix = "+56",
            minDigits = 9,
            maxDigits = 9,
            allowedStartDigits = listOf('9'),
            placeholder = "912345678",
            formatHint = "9 dígitos, inicia con 9"
        ),
        CountryPhoneConfig(
            code = "AR",
            nameEs = "Argentina",
            nameEn = "Argentina",
            flag = "🇦🇷",
            dialPrefix = "+54",
            minDigits = 10,
            maxDigits = 10,
            placeholder = "1123456789",
            formatHint = "10 dígitos"
        ),
        CountryPhoneConfig(
            code = "UY",
            nameEs = "Uruguay",
            nameEn = "Uruguay",
            flag = "🇺🇾",
            dialPrefix = "+598",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('9'),
            placeholder = "99123456",
            formatHint = "8 dígitos, inicia con 9"
        ),
        CountryPhoneConfig(
            code = "PY",
            nameEs = "Paraguay",
            nameEn = "Paraguay",
            flag = "🇵🇾",
            dialPrefix = "+595",
            minDigits = 9,
            maxDigits = 9,
            allowedStartDigits = listOf('9'),
            placeholder = "981234567",
            formatHint = "9 dígitos, inicia con 9"
        ),
        // Caribbean Islands
        CountryPhoneConfig(
            code = "DO",
            nameEs = "República Dominicana",
            nameEn = "Dominican Republic",
            flag = "🇩🇴",
            dialPrefix = "+1809",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5551234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "PR",
            nameEs = "Puerto Rico",
            nameEn = "Puerto Rico",
            flag = "🇵🇷",
            dialPrefix = "+1787",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5551234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "CU",
            nameEs = "Cuba",
            nameEn = "Cuba",
            flag = "🇨🇺",
            dialPrefix = "+53",
            minDigits = 8,
            maxDigits = 8,
            allowedStartDigits = listOf('5'),
            placeholder = "51234567",
            formatHint = "8 dígitos, inicia con 5"
        ),
        CountryPhoneConfig(
            code = "JM",
            nameEs = "Jamaica",
            nameEn = "Jamaica",
            flag = "🇯🇲",
            dialPrefix = "+1876",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5551234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "HT",
            nameEs = "Haití",
            nameEn = "Haiti",
            flag = "🇭🇹",
            dialPrefix = "+509",
            minDigits = 8,
            maxDigits = 8,
            placeholder = "34123456",
            formatHint = "8 dígitos"
        ),
        CountryPhoneConfig(
            code = "TT",
            nameEs = "Trinidad y Tobago",
            nameEn = "Trinidad and Tobago",
            flag = "🇹🇹",
            dialPrefix = "+1868",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5551234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "BS",
            nameEs = "Bahamas",
            nameEn = "Bahamas",
            flag = "🇧🇸",
            dialPrefix = "+1242",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5551234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "BB",
            nameEs = "Barbados",
            nameEn = "Barbados",
            flag = "🇧🇧",
            dialPrefix = "+1246",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5551234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "AW",
            nameEs = "Aruba",
            nameEn = "Aruba",
            flag = "🇦🇼",
            dialPrefix = "+297",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "5921234",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "CW",
            nameEs = "Curazao",
            nameEn = "Curacao",
            flag = "🇨🇼",
            dialPrefix = "+599",
            minDigits = 7,
            maxDigits = 8,
            placeholder = "9123456",
            formatHint = "7-8 dígitos"
        ),
        CountryPhoneConfig(
            code = "BZ",
            nameEs = "Belice",
            nameEn = "Belize",
            flag = "🇧🇿",
            dialPrefix = "+501",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "6123456",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "GY",
            nameEs = "Guyana",
            nameEn = "Guyana",
            flag = "🇬🇾",
            dialPrefix = "+592",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "6123456",
            formatHint = "7 dígitos"
        ),
        CountryPhoneConfig(
            code = "SR",
            nameEs = "Surinam",
            nameEn = "Suriname",
            flag = "🇸🇷",
            dialPrefix = "+597",
            minDigits = 7,
            maxDigits = 7,
            placeholder = "8123456",
            formatHint = "7 dígitos"
        )
    )

    val americanCurrencies = listOf(
        CurrencyOption("NIO", "C$", "Córdoba Nicaragüense", "Nicaraguan Córdoba", "🇳🇮"),
        CurrencyOption("USD", "$", "Dólar Estadounidense", "US Dollar", "🇺🇸"),
        CurrencyOption("MXN", "$", "Peso Mexicano", "Mexican Peso", "🇲🇽"),
        CurrencyOption("COP", "$", "Peso Colombiano", "Colombian Peso", "🇨🇴"),
        CurrencyOption("EUR", "€", "Euro", "Euro", "🇪🇺"),
        CurrencyOption("CAD", "C$", "Dólar Canadiense", "Canadian Dollar", "🇨🇦"),
        CurrencyOption("BRL", "R$", "Real Brasileño", "Brazilian Real", "🇧🇷"),
        CurrencyOption("PEN", "S/", "Sol Peruano", "Peruvian Sol", "🇵🇪"),
        CurrencyOption("CLP", "$", "Peso Chileno", "Chilean Peso", "🇨🇱"),
        CurrencyOption("ARS", "$", "Peso Argentino", "Argentine Peso", "🇦🇷"),
        CurrencyOption("CRC", "₡", "Colón Costarricense", "Costa Rican Colón", "🇨🇷"),
        CurrencyOption("GTQ", "Q", "Quetzal Guatemalteco", "Guatemalan Quetzal", "🇬🇹"),
        CurrencyOption("HNL", "L", "Lempira Hondureña", "Honduran Lempira", "🇭🇳"),
        CurrencyOption("DOP", "RD$", "Peso Dominicano", "Dominican Peso", "🇩🇴"),
        CurrencyOption("UYU", "$", "Peso Uruguayo", "Uruguayan Peso", "🇺🇾"),
        CurrencyOption("BOB", "Bs", "Boliviano", "Bolivian Boliviano", "🇧🇴"),
        CurrencyOption("PYG", "₲", "Guaraní Paraguayo", "Paraguayan Guaraní", "🇵🇾")
    )

    fun validatePhone(country: CountryPhoneConfig, phone: String): Pair<Boolean, String?> {
        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.length < country.minDigits || cleanPhone.length > country.maxDigits) {
            return false to "El número para ${country.nameEs} debe tener ${if (country.minDigits == country.maxDigits) "${country.minDigits}" else "${country.minDigits}-${country.maxDigits}"} dígitos."
        }
        if (country.allowedStartDigits.isNotEmpty()) {
            val firstDigit = cleanPhone.firstOrNull()
            if (firstDigit == null || firstDigit !in country.allowedStartDigits) {
                val allowedStr = country.allowedStartDigits.joinToString(", ")
                return false to "Para ${country.nameEs}, el número debe iniciar con: $allowedStr."
            }
        }
        return true to null
    }
}
