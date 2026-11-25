package com.nplayer.app

import android.app.Application
import com.nplayer.app.player.PlayerManager

/**
 * Application class for N Player.
 * Owns the app-wide PlayerManager (LibVLC + MediaPlayer) so playback
 * survives Activity recreation and can later continue in PiP mode.
 */
class NPlayerApp : Application() {

    lateinit var playerManager: PlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        playerManager = PlayerManager(this)
    }
}
