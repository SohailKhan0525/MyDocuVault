package com.mydocvault.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

@Composable
fun MediaFileViewer(filePath: String, onOpen: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(filePath) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(File(filePath).toUri()))
            prepare()
            playWhenReady = false
        }
    }

    var failed by remember(player) { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(player, lifecycle) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) { failed = true }
        }
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) player.pause() }
        player.addListener(listener)
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            player.removeListener(listener)
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    if (failed) ReaderMessage("This media format could not be played.", "Choose another app", onOpen)
    else AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { PlayerView(it).apply { setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS) } },
        update = { it.player = player }
    )
}
