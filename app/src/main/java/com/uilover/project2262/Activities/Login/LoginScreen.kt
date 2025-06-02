package com.uilover.project2262.Activities.Login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uilover.project2262.R
import com.uilover.project2262.Repository.MainRepository
import com.uilover.project2262.ui.theme.Project2262Theme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
    ) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { MainRepository() }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Button(
                onClick = {
                    when {
                        email.isBlank() -> errorMessage = "Please enter your email"
                        password.isBlank() -> errorMessage = "Please enter your password"
                        else -> {
                            errorMessage = ""

                            scope.launch {
                                try {
                                    val result = repository.loginUser(email, password)

                                    if (result.isSuccess) {
                                        Toast.makeText(
                                            context,
                                            "Login successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onLoginSuccess()
                                    } else {
                                        val exception = result.exceptionOrNull()
                                        errorMessage = when {
                                            exception?.message?.contains(
                                                "password",
                                                true
                                            ) == true ->
                                                "Invalid email or password"

                                            exception?.message?.contains("user", true) == true ->
                                                "No account found with this email"

                                            exception?.message?.contains("network", true) == true ->
                                                "Network error. Please check your connection"

                                            else -> exception?.message
                                                ?: "Login failed. Please try again"
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "An unexpected error occurred. Please try again"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp),
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(
            modifier = Modifier.padding(top = 280.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onRegisterClick
            ) {
                Text("Sign up")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Project2262Theme {
        LoginScreen(
            onLoginSuccess = { },
            onRegisterClick = { }
        )
    }
}