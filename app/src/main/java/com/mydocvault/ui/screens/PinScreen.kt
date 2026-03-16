package com.mydocvault.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
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
    val subheader = if (isCreate) "Choose a 4-digit PIN" else if (!canUnlock) "Loading…" else "Enter your PIN to unlock"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = header,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subheader,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(28.dp))
                PinDots(count = 4, filled = pin.length)
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedVisibility(
                    visible = error.isNotBlank(),
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Keypad section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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
                                            .fillMaxWidth(),
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            modifier = Modifier.size(20.dp)
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
                                        shape = CircleShape
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
                                    pin = ""
                                }
                            }
                        },
                        enabled = isCreate || canUnlock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large,
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
                        Spacer(modifier = Modifier.height(10.dp))
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
                            shape = MaterialTheme.shapes.large
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
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(count) { index ->
            val isFilled = index < filled
            val dotSize by animateDpAsState(
                targetValue = if (isFilled) 16.dp else 12.dp,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                label = "dotSize_$index"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isFilled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                animationSpec = spring(stiffness = 300f),
                label = "dotColor_$index"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
