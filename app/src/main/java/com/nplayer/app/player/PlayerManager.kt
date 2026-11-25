package com.nplayer.app.player

import android.content.Context
import android.net.Uri
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia

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
     * Opens the given video URI and prepares playback (paused until
     * [play] is called or [MediaPlayer.play] is invoked).
     */
    fun openMedia(uri: Uri) {
        currentMedia?.let {
            it.detachViews()
            it.release()
        }
        val media = Media(libVlc, uri)
        media.parseAsync(IMedia.ParseType.ParseLocal)
        currentMedia = media
        mediaPlayer.media = media
    }

    /** Releases LibVLC resources. Call once when the app process ends. */
    fun release() {
        mediaPlayer.stop()
        currentMedia?.release()
        mediaPlayer.release()
        libVlc.release()
    }
}
