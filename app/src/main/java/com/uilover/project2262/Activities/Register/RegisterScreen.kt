package com.uilover.project2262.Activities.Register

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.*
import androidx.compose.material3.Text
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import com.uilover.project2262.R
import com.uilover.project2262.Repository.MainRepository
import kotlinx.coroutines.launch

private fun validateInputs(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    onError: (String) -> Unit // Callback to show error messages
): Boolean {
    return when {
        name.isBlank() -> {
            onError("Please enter your full name")
            false
        }
        email.isBlank() -> {
            onError("Please enter your email")
            false
        }
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
            onError("Please enter a valid email")
            false
        }
        password.isBlank() -> {
            onError("Please enter a password")
            false
        }
        password.length < 6 -> {
            onError("Password must be at least 6 characters")
            false
        }
        confirmPassword.isBlank() -> {
            onError("Please confirm your password")
            false
        }
        password != confirmPassword -> {
            onError("Passwords don't match")
            false
        }
        else -> true
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { MainRepository() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkPurple2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/App Name (similar to your splash screen)
            Text(
                text = "UPlane",
                style = MaterialTheme.typography.displayLarge,
                color = colorResource(id = R.color.orange),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = colorResource(id = R.color.orange),
                            focusedIndicatorColor = colorResource(id = R.color.orange),
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = colorResource(id = R.color.orange),
                            focusedIndicatorColor = colorResource(id = R.color.orange),
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = colorResource(id = R.color.orange),
                            focusedIndicatorColor = colorResource(id = R.color.orange),
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = colorResource(id = R.color.orange),
                            focusedIndicatorColor = colorResource(id = R.color.orange),
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register Button
                    Button(
                        onClick = {
                            errorMessage = null // Reset error
                            if (validateInputs(
                                    name = name,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    onError = { message -> errorMessage = message }
                                )
                            ) {
                                isLoading = true
                                // Proceed with registration
                                scope.launch {
                                    try {
                                        val result = repository.registerUser(name, email, password)

                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                            onRegisterSuccess()
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "An error occurred"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.orange),
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("Register", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Link
                    TextButton(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Login",
                            color = colorResource(id = R.color.orange),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}