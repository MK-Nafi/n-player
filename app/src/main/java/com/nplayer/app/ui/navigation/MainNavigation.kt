package com.nplayer.app.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nplayer.app.data.settings.MainSection
import com.nplayer.app.data.settings.SettingsRepository
import com.nplayer.app.player.PlayerManager
import com.nplayer.app.ui.player.PlayerScreen
import com.nplayer.app.ui.theme.Accent
import com.nplayer.app.ui.theme.BackgroundSecondary
import com.nplayer.app.ui.theme.TextPrimary
import com.nplayer.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class BottomNavItem(
    val section: MainSection,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(MainSection.VIDEO, "Video", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    BottomNavItem(MainSection.AUDIO, "Audio", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    BottomNavItem(MainSection.PLAYLISTS, "Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
    BottomNavItem(MainSection.BROWSE, "Browse", Icons.Filled.Folder, Icons.Outlined.Folder)
)

/**
 * Main navigation host with bottom bar, last-used-section memory, and
 * video playback navigation from the Browse tab.
 */
@Composable
fun MainNavigation(playerManager: PlayerManager) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val savedSection by settingsRepository.lastSection.collectAsState(initial = MainSection.VIDEO)
    var currentSection by remember { mutableStateOf(MainSection.VIDEO) }
    var playingUri by remember { mutableStateOf<Uri?>(null) }
    var playingTitle by remember { mutableStateOf("") }

    // Restore the last-used section once the saved value arrives.
    LaunchedEffect(savedSection) {
        currentSection = savedSection
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF242424)
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentSection == item.section
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            currentSection = item.section
                            scope.launch { settingsRepository.setLastSection(item.section) }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Accent.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentPlayingUri = playingUri
            if (currentPlayingUri != null) {
                PlayerScreen(
                    playerManager = playerManager,
                    videoUri = currentPlayingUri,
                    title = playingTitle,
                    onBack = { playingUri = null }
                )
            } else {
                when (currentSection) {
                    MainSection.VIDEO -> SectionPlaceholder("Video")
                    MainSection.AUDIO -> SectionPlaceholder("Audio")
                    MainSection.PLAYLISTS -> SectionPlaceholder("Playlists")
                    MainSection.BROWSE -> BrowseSection(
                        onVideoPicked = { uri, name ->
                            playingUri = uri
                            playingTitle = name
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowseSection(onVideoPicked: (Uri, String) -> Unit) {
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Non-persistable URIs (e.g. from some file managers) are
                // still playable for this session; ignore.
            }
            val name = queryDisplayName(context, uri) ?: "Video"
            onVideoPicked(uri, name)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Browse",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )
        Text(
            text = "Pick any video file on this device to play it.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    filePicker.launch(arrayOf("video/*"))
                },
            colors = CardDefaults.cardColors(containerColor = BackgroundSecondary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = Accent
                )
                Text(
                    text = "Choose a video file",
                    color = TextPrimary
                )
            }
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(
            uri,
            arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun SectionPlaceholder(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name — coming in the next stage",
            color = TextSecondary
        )
    }
}
