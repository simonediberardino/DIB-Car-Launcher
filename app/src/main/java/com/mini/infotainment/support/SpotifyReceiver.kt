package com.mini.infotainment.support

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mini.infotainment.activities.home.HomeActivity

class SpotifyReceiver : BroadcastReceiver() {
    companion object {
        const val SPOTIFY_PACKAGE = "com.spotify.music"
        const val PLAYBACK_STATE_CHANGED = "$SPOTIFY_PACKAGE.playbackstatechanged"
        const val QUEUE_CHANGED = "$SPOTIFY_PACKAGE.queuechanged"
        const val METADATA_CHANGED = "$SPOTIFY_PACKAGE.metadatachanged"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            METADATA_CHANGED -> {
                HomeActivity.updateSpotifySong(context as Activity, intent)
            }
            PLAYBACK_STATE_CHANGED -> {}
            QUEUE_CHANGED -> {}
        }
    }
}