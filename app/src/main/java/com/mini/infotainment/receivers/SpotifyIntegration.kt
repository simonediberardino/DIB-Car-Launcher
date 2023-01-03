package com.mini.infotainment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.support.SActivity


class SpotifyIntegration : BroadcastReceiver() {
    companion object {
        var lastIntent: Intent? = null
        const val SPOTIFY_PACKAGE = "com.spotify.music"
        const val PLAYBACK_STATE_CHANGED = "$SPOTIFY_PACKAGE.playbackstatechanged"
        const val QUEUE_CHANGED = "$SPOTIFY_PACKAGE.queuechanged"
        const val METADATA_CHANGED = "$SPOTIFY_PACKAGE.metadatachanged"
    }

    private fun handleResumeTrack(context: Context, intent: Intent){
        val delay = 1250L
        HomeActivity.instance?.hasStartedSpotify = true

        Thread{
            Thread.sleep(delay)
            MusicIntegration.resumeTrack(context)
            Thread.sleep(delay)

            if(!isPlayingSong()) {
                handleResumeTrack(context, intent)
            }else{
                if(context is SActivity) {
                    context.log("Starting spotify ${intent.action.toString()}")
                }
            }
        }.start()
    }

    private fun isPlayingSong(): Boolean {
        val audioManager: AudioManager = SActivity.lastActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if(audioManager.isMusicActive)
            return true

        if(lastIntent == null)
            return false

        if(lastIntent!!.getIntExtra("playbackPosition", -1) == -1)
            return false

        if(!lastIntent!!.getBooleanExtra("playing", false))
            return false

        return true
    }

    override fun onReceive(context: Context?, intent: Intent) {
        lastIntent = intent

        when (intent.action) {
            METADATA_CHANGED -> {
                HomeActivity.updateSong(intent)
            }
            PLAYBACK_STATE_CHANGED -> {
                if(HomeActivity.instance?.hasStartedSpotify != true){
                    handleResumeTrack(context!!, intent)
                }
            }
            QUEUE_CHANGED -> {}
        }
    }
}