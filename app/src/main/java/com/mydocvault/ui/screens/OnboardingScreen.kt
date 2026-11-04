package com.mydocvault.ui.screens

import android.animation.ValueAnimator
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import androidx.navigation.NavController
import com.mydocvault.R
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

internal enum class GuidePage(val plate: Int, val title: Int, val body: Int) {
    Filing(R.drawable.guide_filing, R.string.guide_file_title, R.string.guide_file_body),
    Reading(R.drawable.guide_reader, R.string.guide_read_title, R.string.guide_read_body),
    Lock(R.drawable.guide_lock, R.string.guide_lock_title, R.string.guide_lock_body);

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
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .safeDrawingPadding(), contentAlignment = Alignment.Center) {
        val sideBySide = maxWidth >= 600.dp || (maxHeight < 440.dp && maxWidth >= 480.dp)
        val gutter = if (maxWidth < 360.dp) 16.dp else 24.dp
        Column(Modifier.widthIn(max = if (sideBySide) 960.dp else 560.dp)
            .fillMaxSize().padding(horizontal = gutter)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("MyDocuVault", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (page != GuidePage.Lock) TextButton(onClick = onSkip, enabled = !busy) {
                    Text(stringResource(R.string.guide_skip))
                }
            }
            HorizontalDivider()
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
                    if (sideBySide) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(gutter)) {
                            GuidePlate(page, Modifier.weight(1f))
                            GuideCopy(page, Modifier.weight(1f))
                        }
                    } else {
                        GuidePlate(page, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(20.dp))
                        GuideCopy(page)
                    }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (page != GuidePage.Filing) TextButton(onClick = onBack, enabled = !busy) {
                    Text(stringResource(R.string.guide_back))
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onNext, enabled = !busy) {
                    Text(stringResource(if (page == GuidePage.Lock) R.string.guide_pin else R.string.guide_next))
                }
            }
        }
    }
}

@Composable
private fun GuideCopy(page: GuidePage, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(stringResource(R.string.guide_step, page.ordinal + 1, GuidePage.entries.size),
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(page.title), style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif),
            modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(10.dp))
        Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GuidePlate(page: GuidePage, modifier: Modifier) {
    val context = LocalContext.current
    val drawable = remember(context, page) { context.getDrawable(page.plate)!!.mutate() as AnimatedVectorDrawable }
    val owner = LocalLifecycleOwner.current
    var played by rememberSaveable(page) { mutableStateOf(false) }
    AndroidView(modifier = modifier.aspectRatio(256f / 144f).testTag("guide-plate"), factory = {
        ImageView(it).apply {
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageDrawable(drawable)
        }
    }, update = { if (it.drawable !== drawable) it.setImageDrawable(drawable) })
    LaunchedEffect(owner, drawable) {
        owner.lifecycle.withResumed {
            if (!played && ValueAnimator.areAnimatorsEnabled()) drawable.start()
            played = true
        }
    }
    DisposableEffect(drawable) { onDispose { drawable.stop() } }
}
