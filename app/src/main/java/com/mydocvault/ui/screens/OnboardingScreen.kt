package com.mydocvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.R
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.ui.theme.vaultWash
import com.mydocvault.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

internal enum class GuidePage(val title: Int, val body: Int) {
    Filing(R.string.guide_file_title, R.string.guide_file_body),
    Reading(R.string.guide_read_title, R.string.guide_read_body),
    Lock(R.string.guide_lock_title, R.string.guide_lock_body);

    val route get() = "guide/${name.lowercase(java.util.Locale.ROOT)}"
}

@Composable
internal fun OnboardingScreen(navController: NavController, page: GuidePage,
                             viewModel: AuthViewModel = hiltViewModel()) {
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val finish = {
        if (!busy) {
            busy = true
            scope.launch {
                try {
                    viewModel.completeOnboarding()
                    navController.navigate("${NavRoutes.Pin}?mode=create") {
                        popUpTo(NavRoutes.Onboarding) { inclusive = true }
                    }
                } finally {
                    busy = false
                }
            }
        }
    }
    GuideContent(page, busy, onBack = { navController.popBackStack() }, onSkip = finish,
        onNext = {
            val next = GuidePage.entries.getOrNull(page.ordinal + 1)
            if (next == null) finish() else navController.navigate(next.route) { launchSingleTop = true }
        })
}

@Composable
internal fun GuideContent(page: GuidePage, busy: Boolean, onBack: () -> Unit,
                          onSkip: () -> Unit, onNext: () -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxSize().vaultWash().safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        val wide = maxWidth >= 600.dp || (maxHeight < 440.dp && maxWidth >= 480.dp)
        Column(Modifier.widthIn(max = if (wide) 840.dp else 540.dp).fillMaxSize()
            .padding(horizontal = if (maxWidth < 360.dp) 16.dp else 24.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                if (page != GuidePage.Filing) {
                    IconButton(onClick = onBack, enabled = !busy) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.guide_back))
                    }
                }
                Text("MyDocuVault", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (page != GuidePage.Lock) {
                    TextButton(onClick = onSkip, enabled = !busy) { Text(stringResource(R.string.guide_skip)) }
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    if (wide) {
                        Row(Modifier.padding(28.dp), horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            GuideProgress(page, Modifier.weight(0.8f))
                            GuideCopy(page, Modifier.weight(1.2f))
                        }
                    } else {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                            GuideProgress(page)
                            GuideCopy(page)
                        }
                    }
                }
            }
            Button(onClick = onNext, enabled = !busy, shape = CircleShape,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).heightIn(min = 54.dp)) {
                Text(stringResource(if (page == GuidePage.Lock) R.string.guide_pin else R.string.guide_next))
            }
        }
    }
}

@Composable
private fun GuideProgress(page: GuidePage, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.guide_step, page.ordinal + 1, GuidePage.entries.size),
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            GuidePage.entries.forEach { step ->
                Spacer(Modifier.weight(1f).height(6.dp).clip(CircleShape).background(
                    if (step.ordinal <= page.ordinal) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.surfaceVariant))
            }
        }
    }
}

@Composable
private fun GuideCopy(page: GuidePage, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(page.title), modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.displaySmall)
        Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
