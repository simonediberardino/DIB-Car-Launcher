package com.mini.infotainment.receivers

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent

object MusicIntegration {
    fun togglePlayState(ctx: Context){
        sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun nextTrack(ctx: Context) {
        sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun previousTrack(ctx: Context) {
        sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    fun resumeTrack(ctx: Context) {
        sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    fun pauseTrack(ctx: Context) {
        sendEventToTrack(ctx, KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    private fun sendEventToTrack(ctx: Context, keyCode: Int) {
        val mAudioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val eventtime: Long = SystemClock.uptimeMillis()

        val downEvent = KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0)
        mAudioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, keyCode, 0)
        mAudioManager.dispatchMediaKeyEvent(upEvent)
    }
}