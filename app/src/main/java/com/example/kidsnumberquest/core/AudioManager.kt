package com.example.kidsnumberquest.core

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import java.util.Locale

class GameAudio(context: Context) : TextToSpeech.OnInitListener {
    private val tone=ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    private val tts=TextToSpeech(context.applicationContext,this)
    override fun onInit(status:Int){ if(status==TextToSpeech.SUCCESS) tts.language=Locale.US }
    fun correct(){ tone.startTone(ToneGenerator.TONE_PROP_BEEP2,180); tts.speak("Great job!",TextToSpeech.QUEUE_FLUSH,null,"correct") }
    fun speak(text:String){ tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"prompt") }
    fun release(){ tone.release(); tts.shutdown() }
}
