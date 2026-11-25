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
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioTrack
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
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

/**
 * Full-screen video player screen.
 *
 * @param playerManager App-wide player owner.
 * @param videoUri Uri of the selected video.
 * @param title Display title of the video.
 * @param onBack Invoked when the user leaves the player.
 */
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
    var audioTracks by remember { mutableStateOf(listOf<MediaPlayer.TrackDescription>()) }
    var selectedTrackId by remember { mutableStateOf(-1) }

    // VLCVideoLayout provides the SurfaceView video output.
    val videoLayout = remember { VLCVideoLayout(context) }

    DisposableEffect(Unit) {
        playerManager.openMedia(videoUri)
        playerManager.mediaPlayer.attachViews(videoLayout)
        playerManager.mediaPlayer.play()
        onDispose {
            playerManager.mediaPlayer.stop()
            playerManager.mediaPlayer.detachViews()
        }
    }

    // Hide controls after 3 seconds of no interaction.
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    // Poll position/duration 4 times per second.
    LaunchedEffect(Unit) {
        while (true) {
            val player = playerManager.mediaPlayer
            positionMs = player.time
            durationMs = player.length
            isPlaying = player.isPlaying
            delay(250)
        }
    }

    // Load audio tracks once the media has been parsed (duration becomes > 0).
    LaunchedEffect(durationMs) {
        if (durationMs > 0L && audioTracks.isEmpty()) {
            val tracks = playerManager.mediaPlayer.audioTracks
            if (tracks != null && tracks.isNotEmpty()) {
                audioTracks = tracks.toList()
                selectedTrackId = playerManager.mediaPlayer.audioTrack
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

        // Tap layer: toggle controls visibility.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { controlsVisible = !controlsVisible }
        )

        if (controlsVisible) {
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
                    IconButton(
                        onClick = {
                            val tracks = playerManager.mediaPlayer.audioTracks
                            if (tracks != null && tracks.isNotEmpty()) {
                                audioTracks = tracks.toList()
                                selectedTrackId = playerManager.mediaPlayer.audioTrack
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AudioTrack,
                            contentDescription = "Audio tracks",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Seek row: current time, slider, duration.
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
                        onValueChange = { fraction ->
                            val target = (fraction * durationMs).toLong()
                            playerManager.mediaPlayer.time = target
                            positionMs = target
                        },
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
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                playerManager.mediaPlayer.pause()
                            } else {
                                playerManager.mediaPlayer.play()
                            }
                            isPlaying = !isPlaying
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Audio track picker anchored to the bottom end when more than one track.
            if (audioTracks.size > 1) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 96.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    audioTracks.forEach { track ->
                        val name = track.name ?: "Track ${track.id}"
                        Text(
                            text = name,
                            color = if (track.id == selectedTrackId) Accent else Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playerManager.mediaPlayer.setAudioTrack(track.id)
                                    selectedTrackId = track.id
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
