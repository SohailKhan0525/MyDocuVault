package com.mydocvault.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.R
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private data class GuidePage(val plate: Int, val title: Int, val body: Int)
private val guide = listOf(
    GuidePage(R.drawable.plate_filing, R.string.guide_file_title, R.string.guide_file_body),
    GuidePage(R.drawable.plate_reader, R.string.guide_read_title, R.string.guide_read_body),
    GuidePage(R.drawable.plate_lock, R.string.guide_lock_title, R.string.guide_lock_body)
)

@Composable
fun OnboardingScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var step by rememberSaveable { mutableIntStateOf(0) }
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
    BackHandler(step > 0 && !busy) { step-- }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding(),
        contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxSize().padding(horizontal = 24.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("MyDocuVault", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (step < guide.lastIndex) TextButton(onClick = finish, enabled = !busy) { Text(stringResource(R.string.guide_skip)) }
                else Spacer(Modifier.height(48.dp))
            }
            HorizontalDivider()
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                AnimatedContent(targetState = step, label = "Guide page",
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (fadeIn(tween(200)) + slideInHorizontally(tween(240)) { direction * it / 14 })
                            .togetherWith(fadeOut(tween(120)))
                    }) { index ->
                    val page = guide[index]
                    Column(Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
                        Image(painterResource(page.plate), contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(256f / 144f))
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.guide_step, index + 1, guide.size),
                            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(page.title), style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif),
                            modifier = Modifier.semantics { heading() })
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) TextButton(onClick = { step-- }, enabled = !busy) { Text(stringResource(R.string.guide_back)) }
                Spacer(Modifier.weight(1f))
                Button(onClick = { if (step < guide.lastIndex) step++ else finish() }, enabled = !busy) {
                    Text(stringResource(if (step == guide.lastIndex) R.string.guide_pin else R.string.guide_next))
                }
            }
        }
    }
}
