package com.nplayer.app.player

import android.content.Context
import android.net.Uri
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

/**
 * App-wide owner of the LibVLC instance and the media player.
 *
 * Owned by [com.nplayer.app.NPlayerApp] so playback survives Activity
 * recreation (rotation, PiP, background).
 */
class PlayerManager(context: Context) {

    private val libVlc: LibVLC = LibVLC(
        context,
        arrayListOf("--audio-time-stretch", "--avcodec-skiploopfilter", "1")
    )

    val mediaPlayer: MediaPlayer = MediaPlayer(libVlc)

    private var currentMedia: Media? = null

    /**
     * Opens the given video URI and prepares it for playback.
     *
     * VLC parses the media automatically when playback starts; no explicit
     * parse call is needed. The PlayerScreen waits for duration > 0 before
     * reading audio tracks, which only happens once parsing has completed.
     */
    fun openMedia(uri: Uri) {
        currentMedia?.release()
        val media = Media(libVlc, uri)
        currentMedia = media
        mediaPlayer.media = media
    }

    /** Releases LibVLC resources. Call once when the app process ends. */
    fun release() {
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        currentMedia?.release()
        mediaPlayer.release()
        libVlc.release()
    }
}
