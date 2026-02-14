package com.mydocvault.ui.screens

import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.utils.BiometricAuth
import com.mydocvault.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun PinScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val mode = navController.currentBackStackEntry?.arguments?.getString("mode") ?: "unlock"
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val savedPin by viewModel.pinState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (mode == "create") "Create a 4-digit PIN" else "Enter your PIN",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { input ->
                if (input.length <= 4) {
                    pin = input.filter { it.isDigit() }
                    error = ""
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedVisibility(visible = error.isNotBlank()) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            if (pin.length != 4) {
                error = "PIN must be 4 digits"
                return@Button
            }
            if (mode == "create") {
                scope.launch {
                    viewModel.setPin(pin)
                    navController.navigate(NavRoutes.Home) {
                        popUpTo(NavRoutes.Pin) { inclusive = true }
                    }
                }
            } else {
                val current = savedPin
                if (!current.isNullOrBlank() && pin == current) {
                    navController.navigate(NavRoutes.Home) {
                        popUpTo(NavRoutes.Pin) { inclusive = true }
                    }
                } else {
                    error = "Incorrect PIN"
                }
            }
        }, enabled = mode == "create" || !savedPin.isNullOrBlank()) {
            Text(text = if (mode == "create") "Set PIN" else "Unlock")
        }

        if (mode != "create" && biometricEnabled && context is FragmentActivity) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                val biometric = BiometricAuth(context)
                if (biometric.canAuthenticate()) {
                    biometric.authenticate(
                        title = "Unlock MyDocVault",
                        subtitle = "Use your biometric",
                        onSuccess = {
                            navController.navigate(NavRoutes.Home) {
                                popUpTo(NavRoutes.Pin) { inclusive = true }
                            }
                        },
                        onError = { message -> error = message }
                    )
                } else {
                    error = "Biometric not available"
                }
            }) {
                Text("Use biometric")
            }
        }
    }
}
