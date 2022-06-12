package com.mini.infotainment.spotify

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.mini.infotainment.activities.home.HomeActivity
import com.mini.infotainment.support.ActivityExtended

class SpotifyReceiver : BroadcastReceiver() {
    var lastIntent: Intent? = null

    companion object {
        const val SPOTIFY_PACKAGE = "com.spotify.music"
        const val PLAYBACK_STATE_CHANGED = "$SPOTIFY_PACKAGE.playbackstatechanged"
        const val QUEUE_CHANGED = "$SPOTIFY_PACKAGE.queuechanged"
        const val METADATA_CHANGED = "$SPOTIFY_PACKAGE.metadatachanged"

        fun nextSpotifyTrack(ctx: Context) {
            sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_NEXT)
        }

        fun previousSpotifyTrack(ctx: Context) {
            sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }

        fun resumeSpotifyTrack(ctx: Context) {
            sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PLAY)
        }

        fun pauseSpotifyTrack(ctx: Context) {
            sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PAUSE)
        }

        private fun sendEventToTrack(ctx: Context, keyCode: Int) {
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
            intent.setPackage(SPOTIFY_PACKAGE)
            synchronized(this) {
                intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                ctx.sendOrderedBroadcast(intent, null)
                intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyCode))
                ctx.sendOrderedBroadcast(intent, null)
            }
        }
    }

    private fun handleResumeTrack(context: Context, intent: Intent){
        val delay = 1250L
        HomeActivity.homeActivity?.hasStartedSpotify = true

        Thread{
            Thread.sleep(delay)
            resumeSpotifyTrack(context)
            Thread.sleep(delay)

            if(!isPlayingSong()) {
                handleResumeTrack(context, intent)
            }else{
                if(context is ActivityExtended) {
                    context.log("Starting spotify ${intent.action.toString()}")
                }
            }
        }.start()
    }

    private fun isPlayingSong(): Boolean {
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
                HomeActivity.updateSpotifySong(context as Activity, intent)
            }
            PLAYBACK_STATE_CHANGED -> {
                if(HomeActivity.homeActivity?.hasStartedSpotify == false){
                    handleResumeTrack(context!!, intent)
                }
            }
            QUEUE_CHANGED -> {}
        }
    }
}