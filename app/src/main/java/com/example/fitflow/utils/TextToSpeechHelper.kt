package com.example.fitflow.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

class TextToSpeechHelper(context: Context, private val onReady: () -> Unit = {}) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isReady = true
                pendingText?.let { text ->
                    tts?.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "fitflow_tts"
                    )
                }
                pendingText = null
            }
        }
    }

    fun getAvailableVoices(): List<Voice> {
        return tts?.voices?.toList() ?: emptyList()
    }

    fun setVoice(voice: Voice) {
        tts?.voice = voice
    }

    fun speak(text: String) {
        if (!isReady) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fitflow_tts")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
