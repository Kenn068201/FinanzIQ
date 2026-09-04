package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer

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

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Usuario",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
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
                    containerColor = FinanceBackground
                )
            )
        },
        containerColor = FinanceBackground,
        modifier = modifier.fillMaxSize().imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Crea tu cuenta financiera",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            )
            Text(
                text = "Por favor ingresa tus datos con las validaciones requeridas.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4B5563)),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Nombres (Capital + Lowercase)
                    OutlinedTextField(
                        value = firstNames,
                        onValueChange = {
                            firstNames = it
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Nombres (ej. Carlos Alberto)") },
                        placeholder = { Text("Inicia con Mayúscula") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FinancePrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinancePrimary,
                            focusedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_first_name_input")
                    )

                    // 2. Apellidos (Capital + Lowercase)
                    OutlinedTextField(
                        value = lastNames,
                        onValueChange = {
                            lastNames = it
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Apellidos (ej. Mendoza López)") },
                        placeholder = { Text("Inicia con Mayúscula") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FinancePrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinancePrimary,
                            focusedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_last_name_input")
                    )

                    // 3. Edad (Number > 18)
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = {
                            ageStr = it.filter { char -> char.isDigit() }
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Edad (Mayor a 18 años)") },
                        placeholder = { Text("ej. 25") },
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

                    // 4. Número de Teléfono (Starts with 5, 7, 8 and 8 digits)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 8) {
                                phone = it.filter { char -> char.isDigit() }
                            }
                            if (authError != null) viewModel.clearAuthError()
                        },
                        label = { Text("Teléfono (8 dígitos, inicia con 5, 7 u 8)") },
                        placeholder = { Text("ej. 88997766") },
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
                            .fillMaxWidth()
                            .testTag("register_phone_input")
                    )

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
                                color = Color(0xFF374151)
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
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
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
                            text = "Crear Cuenta",
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
                    text = "¿Ya tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4B5563))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Iniciar Sesión",
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
    }
}
