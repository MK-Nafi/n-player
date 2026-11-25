package com.nplayer.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nplayer.app.data.settings.MainSection
import com.nplayer.app.data.settings.SettingsRepository
import com.nplayer.app.ui.theme.Accent
import com.nplayer.app.ui.theme.TextSecondary

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
 * Main navigation host with bottom bar and last-used-section memory.
 */
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }

    val savedSection by settingsRepository.lastSection
        .collectAsStateWithLifecycle(initialValue = MainSection.VIDEO)
    var currentSection by remember { androidx.compose.runtime.mutableStateOf(MainSection.VIDEO) }

    // Restore the last-used section once on startup.
    LaunchedEffect(Unit) {
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
                            // Persist the choice (fire and forget).
                            kotlinx.coroutines.MainScope().launch {
                                settingsRepository.setLastSection(item.section)
                            }
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
            when (currentSection) {
                MainSection.VIDEO -> VideoSectionPlaceholder()
                MainSection.AUDIO -> AudioSectionPlaceholder()
                MainSection.PLAYLISTS -> PlaylistsSectionPlaceholder()
                MainSection.BROWSE -> BrowseSectionPlaceholder()
            }
        }
    }
}

@Composable
private fun VideoSectionPlaceholder() {
    SectionPlaceholder("Video")
}

@Composable
private fun AudioSectionPlaceholder() {
    SectionPlaceholder("Audio")
}

@Composable
private fun PlaylistsSectionPlaceholder() {
    SectionPlaceholder("Playlists")
}

@Composable
private fun BrowseSectionPlaceholder() {
    SectionPlaceholder("Browse")
}

@Composable
private fun SectionPlaceholder(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "$name — coming in the next stage",
            color = TextSecondary
        )
    }
}

private fun kotlinx.coroutines.MainScope().launch(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) =
    kotlinx.coroutines.launch(block)
