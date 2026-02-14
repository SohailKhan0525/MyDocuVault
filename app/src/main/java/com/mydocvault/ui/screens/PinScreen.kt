package com.mydocvault.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.utils.BiometricAuth
import com.mydocvault.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalFoundationApi::class)
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

    val isCreate = mode == "create"
    val canUnlock = !savedPin.isNullOrBlank()
    val header = if (isCreate) "Create PIN" else "Welcome back"
    val subheader = if (isCreate) "Set a 4-digit lock" else if (!canUnlock) "Loading PIN..." else "Enter your PIN"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = header, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = subheader, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))
                PinDots(count = 4, filled = pin.length)
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(visible = error.isNotBlank()) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "back")
                        items(keys) { key ->
                            when (key) {
                                "" -> Spacer(modifier = Modifier.aspectRatio(1f))
                                "back" -> {
                                    FilledTonalButton(
                                        onClick = {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            error = ""
                                        },
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                                    }
                                }
                                else -> {
                                    FilledTonalButton(
                                        onClick = {
                                            if (pin.length < 4) {
                                                pin += key
                                                error = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxWidth()
                                    ) {
                                        Text(text = key, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (pin.length != 4) {
                                error = "PIN must be 4 digits"
                                return@Button
                            }
                            if (isCreate) {
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
                        },
                        enabled = isCreate || canUnlock,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = if (isCreate) "Set PIN" else "Unlock")
                    }

                    if (!isCreate && biometricEnabled && context is FragmentActivity) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
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
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Use biometric")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDots(count: Int, filled: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index < filled) 14.dp else 12.dp)
                    .background(
                        color = if (index < filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )
        }
    }
}
