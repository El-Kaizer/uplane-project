package com.uilover.project2262.Activities.Register

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.uilover.project2262.Activities.Login.LoginActivity
import com.uilover.project2262.ui.theme.Project2262Theme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Project2262Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            // After successful registration, go to login
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        },
                        onLoginClick = {
                            // If user wants to login instead
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}