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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioTrack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.viewinterop.AndroidView
import com.nplayer.app.player.PlayerManager
import com.nplayer.app.ui.theme.Accent
import com.nplayer.app.ui.theme.BackgroundSecondary
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File
import kotlin.math.roundToInt

/**
 * Full-screen video player screen.
 *
 * @param playerManager App-wide player owner.
 * @param videoUri Uri of the selected video.
 * @param title Display title of the video.
 * @param onBack Invoked when the user leaves the player.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    var positionMs by remember { mutableStateOf(0f) }
    var durationMs by remember { mutableStateOf(0f) }
    var audioTracks by remember { mutableStateOf(listOf<MediaPlayer.TrackDescription>()) }
    var selectedTrackId by remember { mutableStateOf(-1) }

    // Attach a SurfaceView for video output.
    val videoLayout = remember { VLCVideoLayout(context) }
    DisposableEffect(Unit) {
        playerManager.openMedia(videoUri)
        playerManager.mediaPlayer.attachViews(videoLayout)
        playerManager.mediaPlayer.play()
        onDispose {
            playerManager.mediaPlayer.stop()
            playerManager.mediaPlayer.detachViews()
        }
        return@DisposableEffect onDispose {
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
            positionMs = player.time.toFloat()
            durationMs = player.length.toFloat()
            isPlaying = player.isPlaying
            delay(250)
        }
    }

    // Load audio tracks once the media is parsed (after playback starts).
    LaunchedEffect(durationMs) {
        if (durationMs > 0f && audioTracks.isEmpty()) {
            val tracks = playerManager.mediaPlayer.audioTracks ?: emptyArray()
            audioTracks = tracks.toList()
            selectedTrackId = playerManager.mediaPlayer.getAudioTrack() ?: -1
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

        // Tap layer: tap to show/hide controls.
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
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = {
                            val tracks = playerManager.mediaPlayer.audioTracks
                            if (tracks != null && tracks.isNotEmpty()) {
                                selectedTrackId = playerManager.mediaPlayer.getAudioTrack() ?: -1
                                audioTracks = tracks.toList()
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

                Spacer(Modifier.weight(1f))

                // Seek row: time, slider, duration.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(positionMs),
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = if (durationMs > 0f) positionMs / durationMs else 0f,
                        onValueChange = { fraction ->
                            val target = (fraction * durationMs).toLong()
                            playerManager.mediaPlayer.time = target
                            positionMs = target.toFloat()
                        },
                        onValueChangeFinished = {},
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
                        com.nplayer.app.ui.theme.TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
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

            // Audio track sheet anchored to bottom-right when tracks exist.
            if (audioTracks.size > 1) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 96.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    audioTracks.forEach { track ->
                        Text(
                            text = track.name,
                            color = if (track.id == selectedTrackId) Accent else Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
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

private fun formatTime(ms: Float): String {
    if (ms.isNaN() || ms <= 0f) return "0:00"
    val totalSeconds = (ms / 1000f).roundToInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds ** 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.totalSeconds % 60
    }
}

private val Int.dp: androidx.compose.ui.unit.Dp
    get() = androidx.compose.ui.unit.Dp(this.toFloat())

private fun Spacer(weight: Float): androidx.compose.foundation.layout.Spacer {
    return androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
}
