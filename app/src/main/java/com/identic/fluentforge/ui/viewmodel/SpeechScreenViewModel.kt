package com.identic.fluentforge.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import java.util.*


@HiltViewModel
class SpeechScreenViewModel @Inject constructor(private val app: Application) : ViewModel() {
    private var phrasesList = emptyList<String>()
    var loadedPhrase = mutableStateOf("")
    var isBtnEnabled = mutableStateOf(true)

    init {
        viewModelScope.launch {
            readPhrases()
            selectRandomFromList()
        }
    }

    fun readPhrases() {
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
}

