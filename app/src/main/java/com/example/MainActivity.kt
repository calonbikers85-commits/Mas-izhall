package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.auth.AuthMainScreen
import com.example.ui.screens.customer.CustomerMainScreen
import com.example.ui.screens.mechanic.MechanicMainScreen
import com.example.ui.theme.BengkelkuTheme
import com.example.viewmodel.BengkelViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BengkelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BengkelkuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BengkelkuApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BengkelkuApp(viewModel: BengkelViewModel) {
    val authState by viewModel.authState.collectAsState()

    when (authState.currentRole) {
        null -> {
            AuthMainScreen(viewModel = viewModel)
        }
        UserRole.CUSTOMER -> {
            CustomerMainScreen(viewModel = viewModel)
        }
        UserRole.MECHANIC -> {
            MechanicMainScreen(viewModel = viewModel)
        }
        UserRole.ADMIN -> {
            AdminMainScreen(viewModel = viewModel)
        }
    }
}
