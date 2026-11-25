package com.nplayer.app.ui.player

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nplayer.app.player.PlayerManager
import com.nplayer.app.ui.theme.Accent
import com.nplayer.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import org.videolan.libvlc.util.VLCVideoLayout
import java.util.Locale

@Composable
fun PlayerScreen(
    playerManager: PlayerManager,
    videoUri: Uri,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }
    var positionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var audioTracks by remember { mutableStateOf(listOf<Pair<Int, String>>()) }
    var selectedTrackId by remember { mutableStateOf(-1) }

    val videoLayout = remember { VLCVideoLayout(context) }

    LaunchedEffect(videoUri) {
        val opened = playerManager.openMedia(context, videoUri)
        if (opened) {
            playerManager.mediaPlayer.play()
        } else {
            errorMessage = "Could not open this file. It may be unsupported."
        }
    }

    DisposableEffect(Unit) {
        playerManager.mediaPlayer.attachViews(videoLayout, null, false, false)
        onDispose {
            playerManager.mediaPlayer.stop()
            playerManager.mediaPlayer.detachViews()
        }
    }

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    LaunchedEffect(videoUri) {
        while (true) {
            val player = playerManager.mediaPlayer
            positionMs = player.time
            durationMs = player.length
            isPlaying = player.isPlaying
            delay(250)
        }
    }

    LaunchedEffect(durationMs) {
        if (durationMs > 0L && audioTracks.isEmpty()) {
            refreshAudioTracks(playerManager) { tracks, selected ->
                audioTracks = tracks
                selectedTrackId = selected
            }
        }
    }

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { videoLayout },
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        } else {
            PlayerControls(
                playerManager = playerManager,
                title = title,
                isPlaying = isPlaying,
                controlsVisible = controlsVisible,
                positionMs = positionMs,
                durationMs = durationMs,
                audioTracks = audioTracks,
                selectedTrackId = selectedTrackId,
                onBack = onBack,
                onToggleControls = { controlsVisible = !controlsVisible },
                onTogglePlay = {
                    if (isPlaying) {
                        playerManager.mediaPlayer.pause()
                    } else {
                        playerManager.mediaPlayer.play()
                    }
                    isPlaying = !isPlaying
                },
                onSelectTrack = { trackId ->
                    playerManager.mediaPlayer.setAudioTrack(trackId)
                    selectedTrackId = trackId
                },
                onSeek = { fraction ->
                    val target = (fraction * durationMs).toLong()
                    playerManager.mediaPlayer.time = target
                    positionMs = target
                },
                onRefreshTracks = {
                    refreshAudioTracks(playerManager) { tracks, selected ->
                        audioTracks = tracks
                        selectedTrackId = selected
                    }
                }
            )
        }
    }
}

@Composable
private fun PlayerControls(
    playerManager: PlayerManager,
    title: String,
    isPlaying: Boolean,
    controlsVisible: Boolean,
    positionMs: Long,
    durationMs: Long,
    audioTracks: List<Pair<Int, String>>,
    selectedTrackId: Int,
    onBack: () -> Unit,
    onToggleControls: () -> Unit,
    onTogglePlay: () -> Unit,
    onSelectTrack: (Int) -> Unit,
    onSeek: (Float) -> Unit,
    onRefreshTracks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggleControls() }
    ) {
        if (!controlsVisible) return@Box

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                IconButton(onClick = onRefreshTracks) {
                    Icon(
                        imageVector = Icons.Filled.Audiotrack,
                        contentDescription = "Audio tracks",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(positionMs),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = if (durationMs > 0L) positionMs.toFloat() / durationMs else 0f,
                    onValueChange = onSeek,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Text(
                    text = formatTime(durationMs),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        if (audioTracks.size > 1) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                audioTracks.forEach { (trackId, trackName) ->
                    Text(
                        text = trackName,
                        color = if (trackId == selectedTrackId) Accent else Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTrack(trackId) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

private fun refreshAudioTracks(
    playerManager: PlayerManager,
    onUpdate: (List<Pair<Int, String>>, Int) -> Unit
) {
    val tracks = playerManager.mediaPlayer.audioTracks
    if (tracks != null && tracks.isNotEmpty()) {
        val selected = playerManager.mediaPlayer.audioTrack
        onUpdate(tracks.map { it.id to (it.name ?: "Track ${it.id}") }, selected)
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
