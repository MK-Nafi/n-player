package com.nplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import com.nplayer.app.ui.navigation.MainNavigation
import com.nplayer.app.ui.theme.BackgroundPrimary
import com.nplayer.app.ui.theme.NPlayerTheme
import com.nplayer.app.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NPlayerTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    TopAppBar(
                        title = { Text(text = "N Player", color = TextPrimary) },
                        actions = {
                            IconButton(onClick = { /* Search — Stage 4 */ }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = TextPrimary
                                )
                            }
                            IconButton(onClick = { /* Options menu — Stage 4 */ }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                    tint = TextPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BackgroundPrimary,
                            titleContentColor = TextPrimary
                        )
                    )
                    MainNavigation()
                }
            }
        }
    }
}
