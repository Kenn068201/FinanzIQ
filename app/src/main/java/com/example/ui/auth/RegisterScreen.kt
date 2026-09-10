package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.util.CountryPhoneConfig
import com.example.util.CountryPhoneData
import com.example.util.CurrencyOption
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var firstNames by remember { mutableStateOf("") }
    var lastNames by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") } // "USER" or "ADMIN"

    // Country prefix selector (North, Central, South America and Caribbean)
    var selectedCountry by remember { mutableStateOf(CountryPhoneData.americanCountries.first()) }
    var showCountryDialog by remember { mutableStateOf(false) }

    // Currency selector
    var selectedCurrency by remember { mutableStateOf(CountryPhoneData.americanCurrencies.first()) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.t("register_title", currentLang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = FinancePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize().imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = Localization.t("register_subtitle", currentLang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Ingresa tus datos con las validaciones requeridas de la plataforma.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            // Red Error Warning Box
            AnimatedVisibility(visible = authError != null) {
                authError?.let { err ->
                    Column {
                        ErrorWarningBox(
                            message = err,
                            testTag = "register_error_box"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Nombres (Capital + Lowercase, Max 70 chars)
                    Column {
                        OutlinedTextField(
                            value = firstNames,
                            onValueChange = {
                                if (it.length <= 70) {
                                    firstNames = it
                                    if (authError != null) viewModel.clearAuthError()
                                }
                            },
                            label = { Text(Localization.t("first_names", currentLang)) },
                            placeholder = { Text("Inicia con Mayúscula (máx. 70 carac.)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = FinancePrimary)
                            },
                            trailingIcon = {
                                Text(
                                    text = "${firstNames.length}/70",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (firstNames.length >= 70) FinanceError else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            isError = firstNames.length >= 70,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinancePrimary,
                                focusedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_first_name_input")
                        )
                        if (firstNames.length >= 70) {
                            Text(
                                text = "Has alcanzado el límite máximo de 70 caracteres para este campo.",
                                style = MaterialTheme.typography.labelSmall.copy(color = FinanceError),
                                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                            )
                        }
                    }

                    // 2. Apellidos (Capital + Lowercase, Max 70 chars)
                    Column {
                        OutlinedTextField(
                            value = lastNames,
                            onValueChange = {
                                if (it.length <= 70) {
                                    lastNames = it
                                    if (authError != null) viewModel.clearAuthError()
                                }
                            },
                            label = { Text(Localization.t("last_names", currentLang)) },
                            placeholder = { Text("Inicia con Mayúscula (máx. 70 carac.)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = FinancePrimary)
                            },
                            trailingIcon = {
                                Text(
                                    text = "${lastNames.length}/70",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (lastNames.length >= 70) FinanceError else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            isError = lastNames.length >= 70,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinancePrimary,
                                focusedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_last_name_input")
                        )
                        if (lastNames.length >= 70) {
                            Text(
                                text = "Has alcanzado el límite máximo de 70 caracteres para este campo.",
                                style = MaterialTheme.typography.labelSmall.copy(color = FinanceError),
                                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                            )
                        }
                    }

                    // 3. Edad (Number > 18)
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = {
                            ageStr = it.filter { char -> char.isDigit() }
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text(Localization.t("age", currentLang)) },
                        placeholder = { Text("ej. 25 (Mayor a 18 años)") },
                        leadingIcon = {
                            Icon(Icons.Default.Cake, contentDescription = null, tint = FinancePrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinancePrimary,
                            focusedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_age_input")
                    )

                    // 4. Country Prefix & Phone Segmenter
                    Column {
                        Text(
                            text = "Prefijo Telefónico y Número:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Country Prefix Picker Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showCountryDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 14.dp)
                                    .testTag("country_prefix_selector"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${selectedCountry.flag} ${selectedCountry.dialPrefix}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar País",
                                        tint = FinancePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Phone Input
                            OutlinedTextField(
                                value = phone,
                                onValueChange = {
                                    val digitsOnly = it.filter { c -> c.isDigit() }
                                    if (digitsOnly.length <= selectedCountry.maxDigits) {
                                        phone = digitsOnly
                                        if (authError != null) viewModel.clearAuthError()
                                    }
                                },
                                label = { Text("Teléfono (${if (currentLang == com.example.util.AppLanguage.SPANISH) selectedCountry.nameEs else selectedCountry.nameEn})") },
                                placeholder = { Text(selectedCountry.formatHint) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = FinancePrimary)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FinancePrimary,
                                    focusedLabelColor = FinancePrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("register_phone_input")
                            )
                        }

                        // Hint for country format
                        val countryName = if (currentLang == com.example.util.AppLanguage.SPANISH) selectedCountry.nameEs else selectedCountry.nameEn
                        Text(
                            text = "Formato ($countryName): ${selectedCountry.maxDigits} dígitos" +
                                    if (selectedCountry.allowedStartDigits.isNotEmpty()) ", inicia con ${selectedCountry.allowedStartDigits.joinToString(", ")}" else "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    // Currency Selector Segmenter
                    Column {
                        Text(
                            text = "Moneda Preferida:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showCurrencyDialog = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("currency_selector_box")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = FinancePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${selectedCurrency.flag} ${selectedCurrency.symbol} - ${selectedCurrency.code}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = if (currentLang == com.example.util.AppLanguage.SPANISH) selectedCurrency.nameEs else selectedCurrency.nameEn,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = FinancePrimary
                                )
                            }
                        }
                    }

                    // 5. Correo Electrónico (usuario@dominio.com)
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Correo Electrónico ('usuario@dominio.com')") },
                        placeholder = { Text("usuario@dominio.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = FinancePrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinancePrimary,
                            focusedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input")
                    )

                    // 6. Contraseña (>= 8 chars, 1 uppercase, 1 digit, 1 special char)
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Contraseña") },
                        placeholder = { Text("8-20 carac., Mayús, Número, Especial") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = FinancePrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinancePrimary,
                            focusedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    // 7. Confirmación de Contraseña
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Confirmar Contraseña") },
                        placeholder = { Text("Repite exactamente tu contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = FinancePrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinancePrimary,
                            focusedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirm_password_input")
                    )

                    // Tipo de Usuario (Normal vs Administrador)
                    Column {
                        Text(
                            text = "Tipo de Cuenta:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = selectedRole == "USER",
                                onClick = { selectedRole = "USER" },
                                label = { Text("Usuario Normal") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinanceSecondaryContainer,
                                    selectedLabelColor = FinancePrimary
                                ),
                                modifier = Modifier.testTag("role_user_chip")
                            )

                            FilterChip(
                                selected = selectedRole == "ADMIN",
                                onClick = { selectedRole = "ADMIN" },
                                label = { Text("Administrador") },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinanceSecondaryContainer,
                                    selectedLabelColor = FinancePrimary
                                ),
                                modifier = Modifier.testTag("role_admin_chip")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit Registration Button
                    Button(
                        onClick = {
                            viewModel.register(
                                firstNames = firstNames,
                                lastNames = lastNames,
                                ageStr = ageStr,
                                phone = phone,
                                countryConfig = selectedCountry,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                preferredCurrency = selectedCurrency,
                                role = selectedRole,
                                onSuccess = onRegisterSuccess
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FinancePrimary,
                            contentColor = FinanceOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit_button")
                    ) {
                        Text(
                            text = Localization.t("register_button", currentLang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Back to Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.t("already_have_account", currentLang),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = Localization.t("login_title", currentLang),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = FinancePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(4.dp)
                        .testTag("navigate_login_button")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Country Prefix Selection Dialog
        if (showCountryDialog) {
            AlertDialog(
                onDismissRequest = { showCountryDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = FinancePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prefijo Telefónico (América)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(CountryPhoneData.americanCountries) { country ->
                            val isSelected = country.dialPrefix == selectedCountry.dialPrefix && country.code == selectedCountry.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FinanceSecondaryContainer else Color.Transparent)
                                    .clickable {
                                        selectedCountry = country
                                        // Clear phone if it doesn't match new length
                                        if (phone.length > country.maxDigits) {
                                            phone = phone.take(country.maxDigits)
                                        }
                                        showCountryDialog = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = country.flag,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        val cName = if (currentLang == com.example.util.AppLanguage.SPANISH) country.nameEs else country.nameEn
                                        Text(
                                            text = "${country.dialPrefix} ($cName)",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "${country.maxDigits} dígitos • ${country.formatHint}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = FinancePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCountryDialog = false }) {
                        Text("Cerrar")
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // Currency Selection Dialog
        if (showCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showCurrencyDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = FinancePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Moneda del Usuario",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(CountryPhoneData.americanCurrencies) { curr ->
                            val isSelected = curr.code == selectedCurrency.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FinanceSecondaryContainer else Color.Transparent)
                                    .clickable {
                                        selectedCurrency = curr
                                        viewModel.setCurrency(curr)
                                        showCurrencyDialog = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = curr.flag,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${curr.symbol} ${curr.code}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = if (currentLang == com.example.util.AppLanguage.SPANISH) curr.nameEs else curr.nameEn,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = FinancePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCurrencyDialog = false }) {
                        Text("Cerrar")
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
