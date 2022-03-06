package com.mini.infotainment.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SpotifyReceiver : BroadcastReceiver() {
    internal object BroadcastTypes {
        const val SPOTIFY_PACKAGE = "com.spotify.music"
        const val PLAYBACK_STATE_CHANGED = "$SPOTIFY_PACKAGE.playbackstatechanged"
        const val QUEUE_CHANGED = "$SPOTIFY_PACKAGE.queuechanged"
        const val METADATA_CHANGED = "$SPOTIFY_PACKAGE.metadatachanged"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            BroadcastTypes.METADATA_CHANGED -> {
                HomeActivity.updateSpotifySong(intent)
            }
            BroadcastTypes.PLAYBACK_STATE_CHANGED -> {}
            BroadcastTypes.QUEUE_CHANGED -> {}
        }
    }
}