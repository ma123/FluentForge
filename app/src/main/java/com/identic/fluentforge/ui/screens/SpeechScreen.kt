package com.identic.fluentforge.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.identic.fluentforge.MainActivity
import com.identic.fluentforge.common.Utils
import com.identic.fluentforge.ui.theme.DarkGrey
import com.identic.fluentforge.ui.viewmodel.SpeechScreenViewModel
import java.util.Locale

@Composable
fun SpeechScreen(
    speechScreenViewModel: SpeechScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val speechContext = context as MainActivity

    val loadedPhrase by remember { speechScreenViewModel.loadedPhrase }
    var isBtnEnabled by remember { speechScreenViewModel.isBtnEnabled }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = loadedPhrase,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(18.dp),
                color = DarkGrey
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    runTextToSpeech(
                        context,
                        loadedPhrase,
                        isBtnEnabled,
                        speechScreenViewModel
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Listen"
                )
            }

            Text(
                text = speechContext.speechInput.value,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(18.dp),
                color = DarkGrey
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    speechContext.speechInput.value = ""
                    speechContext.askSpeechInput(context)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Speak"
                )
            }

            if (speechContext.speechInput.value.isNotEmpty()) {
                Text(
                    text = Utils.levenshteinDistancePercent(
                        loadedPhrase,
                        speechContext.speechInput.value
                    ).toString() + " %",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(18.dp),
                    color = DarkGrey
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    speechContext.speechInput.value = ""
                    speechScreenViewModel.selectRandomFromList()
                    runTextToSpeech(
                        context,
                        loadedPhrase,
                        isBtnEnabled,
                        speechScreenViewModel
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Next"
                )
            }
        }
    }
}

fun runTextToSpeech(
    context: Context,
    loadedPhrase: String,
    isBtnEnabled: Boolean,
    speechScreenViewModel: SpeechScreenViewModel
) {
    var tts: TextToSpeech? = null
    if (isBtnEnabled) {
        speechScreenViewModel.isBtnEnabled.value = false
        tts = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.let { txtToSpeech ->
                    txtToSpeech.language = Locale.ENGLISH
                    txtToSpeech.setPitch(1f)
                    txtToSpeech.setSpeechRate(1f)
                    txtToSpeech.speak(
                        loadedPhrase,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        null
                    )
                }
            }
        }
    }
    speechScreenViewModel.isBtnEnabled.value = true
}