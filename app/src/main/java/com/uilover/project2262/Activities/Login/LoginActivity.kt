package com.uilover.project2262.Activities.Login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import com.uilover.project2262.Activities.Dashboard.DashboardActivity
import com.uilover.project2262.ui.theme.Project2262Theme
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.uilover.project2262.Activities.Register.RegisterActivity

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Project2262Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginSuccess = {  startActivity(Intent(this, DashboardActivity::class.java))},
                        onRegisterClick = { startActivity(Intent(this, RegisterActivity::class.java))}
                    )
                }
            }
        }
    }
}
