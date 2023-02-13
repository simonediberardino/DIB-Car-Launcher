package com.mini.infotainment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager


class VolumeChangedReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val am = p0?.getSystemService(AUDIO_SERVICE) as AudioManager?
        val volumeLevel = am!!.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
}