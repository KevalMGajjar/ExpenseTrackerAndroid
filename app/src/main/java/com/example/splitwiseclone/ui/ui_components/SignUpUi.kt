package com.example.splitwiseclone.ui.ui_components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.rest_api.models.User
import com.example.splitwiseclone.ui_viewmodels.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpUi(
    navHostController: NavHostController,
    signUpViewModel: SignUpViewModel = hiltViewModel(),
    userApiViewModel: UserApiViewModel = hiltViewModel()
) {
    val username by signUpViewModel.username
    val email by signUpViewModel.email
    val password by signUpViewModel.password
    val currencyCode by signUpViewModel.currencyCode
    val phoneNumber by signUpViewModel.phoneNumber
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Get error states from the ViewModel
    val usernameError by signUpViewModel.usernameError
    val emailError by signUpViewModel.emailError
    val passwordError by signUpViewModel.passwordError
    val phoneNumberError by signUpViewModel.phoneNumberError

    val registrationSuccess by userApiViewModel.registrationSuccess.collectAsState()
    val registrationError by userApiViewModel.registrationError.collectAsState()

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            isLoading = false
            navHostController.navigate("dashboard") { popUpTo("welcome") { inclusive = true } }
            userApiViewModel.resetRegistrationStatus()
        }
    }
    LaunchedEffect(registrationError) {
        if (registrationError != null) {
            isLoading = false
            // Here you can show a Snackbar with the registrationError message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = { IconButton(onClick = { navHostController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Join and start sharing", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { signUpViewModel.storeUsername(it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                isError = usernameError != null,
                supportingText = { if (usernameError != null) Text(usernameError!!) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { signUpViewModel.storeEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = { if (emailError != null) Text(emailError!!) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { signUpViewModel.storePassword(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = passwordError != null,
                supportingText = { if (passwordError != null) Text(passwordError!!) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Done else Icons.Filled.Clear
                    IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, "Toggle password visibility") }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { signUpViewModel.storePhoneNumber(it) },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = phoneNumberError != null,
                supportingText = { if (phoneNumberError != null) Text(phoneNumberError!!) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currencyCode,
                onValueChange = { signUpViewModel.storeCurrencyCode(it) },
                label = { Text("Currency Code (e.g., USD, INR)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Add, null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (signUpViewModel.validateInputs()) {
                        isLoading = true
                        userApiViewModel.registerUser(
                            User(username = username, email = email, password = password, currencyCode = currencyCode, phoneNumber = phoneNumber)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign Up")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?")
                TextButton(onClick = { navHostController.navigate("login") }) { Text("Log In") }
            }
        }
    }
}