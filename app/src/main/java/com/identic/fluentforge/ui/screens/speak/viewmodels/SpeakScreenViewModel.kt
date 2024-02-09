package com.identic.fluentforge.ui.screens.speak.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identic.fluentforge.common.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import java.util.Random
import javax.inject.Inject


@HiltViewModel
class SpeakScreenViewModel @Inject constructor(private val app: Application) : ViewModel() {
    private var phrasesList = emptyList<String>()
    private var tts: TextToSpeech? = null
    var loadedPhrase = mutableStateOf("")
    var isBtnEnabled = mutableStateOf(true)
    var percentMatch = mutableIntStateOf(0)
    var textFromSpeech = mutableStateOf("")

    init {
        viewModelScope.launch {
            readPhrases()
            selectRandomFromList()
            runTextToSpeech(app.applicationContext)
        }
    }

    private fun readPhrases() {
        phrasesList = readLinesFromAssets("en_phrases.txt")
    }

    fun selectRandomFromList() {
        if (phrasesList.isNotEmpty()) {
            val randomIndex = Random().nextInt(phrasesList.size)
            loadedPhrase.value = phrasesList[randomIndex]
        } else {
            loadedPhrase.value = "error"
        }
    }

    private fun readLinesFromAssets(fileName: String): List<String> {
        val lines = mutableListOf<String>()
        try {
            val inputStream = app.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

            var line: String? = bufferedReader.readLine()
            while (line != null) {
                lines.add(line)
                line = bufferedReader.readLine()
            }

            bufferedReader.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return lines
    }

    fun runTextToSpeech(
        context: Context
    ) {
        if (isBtnEnabled.value) {
            isBtnEnabled.value = false
            tts = TextToSpeech(
                context
            ) {
                if (it == TextToSpeech.SUCCESS) {
                    tts?.let { txtToSpeech ->
                        txtToSpeech.language = Locale.ENGLISH
                        txtToSpeech.setPitch(1f)
                        txtToSpeech.setSpeechRate(1f)
                        txtToSpeech.speak(
                            loadedPhrase.value,
                            TextToSpeech.QUEUE_ADD,
                            null,
                            null
                        )
                    }
                }
            }
        }
        isBtnEnabled.value = true
    }

    fun calculateLevensteinDistance(loadedPhrase: String, speechInput: String) {
        percentMatch.intValue = Utils.levenshteinDistancePercent(
            loadedPhrase,
            speechInput
        )
    }

    fun startSpeechToText(context: Context, finished: () -> Unit) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )

        // Optionally I have added my mother language
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray?) {}
            override fun onEndOfSpeech() {
                // changing the color of your mic icon to
                // gray to indicate it is not listening or do something you want
                finished()
            }

            override fun onError(i: Int) {}

            override fun onResults(bundle: Bundle) {
                val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (result != null) {
                    textFromSpeech.value = result[0]
                }
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle?) {}

        })
        speechRecognizer.startListening(speechRecognizerIntent)
    }
}

