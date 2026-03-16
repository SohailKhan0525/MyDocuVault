package com.mydocvault.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
    val subheader = if (isCreate) "Set a 4-digit PIN to protect your vault" else if (!canUnlock) "Loading…" else "Enter your PIN to unlock"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        1f to MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = header,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subheader,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                PinDots(count = 4, filled = pin.length)
                Spacer(modifier = Modifier.height(12.dp))
                if (error.isNotBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Keypad section
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
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
                                    FilledTonalIconButton(
                                        onClick = {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            error = ""
                                        },
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxWidth(),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            modifier = Modifier.size(22.dp)
                                        )
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
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
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
                                    pin = ""
                                }
                            }
                        },
                        enabled = isCreate || canUnlock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isCreate) "Set PIN" else "Unlock",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (!isCreate && biometricEnabled && context is FragmentActivity) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Use biometric",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDots(count: Int, filled: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(count) { index ->
            val isFilled = index < filled
            val dotSize by animateDpAsState(
                targetValue = if (isFilled) 16.dp else 12.dp,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "dot_size_$index"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isFilled) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.outline,
                label = "dot_color_$index"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(color = dotColor, shape = CircleShape)
            )
        }
    }
}
