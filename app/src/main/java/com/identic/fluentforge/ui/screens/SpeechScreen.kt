package com.identic.fluentforge.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.identic.fluentforge.ui.viewmodel.SpeechScreenViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SpeechScreen(
    vm: SpeechScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val loadedPhrase by remember { vm.loadedPhrase }
    val isBtnEnabled by remember { vm.isBtnEnabled }
    val percentMatch by remember { vm.percentMatch }
    val textFromSpeech by remember { vm.textFromSpeech }
    var micColor by remember { mutableStateOf(Color.Blue) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Scaffold(bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (textFromSpeech.isNotEmpty()) {
                    vm.calculateLevensteinDistance(
                        loadedPhrase,
                        textFromSpeech
                    )

                    Text(
                        text = "$percentMatch %",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(1F),
                        onClick = {
                        },
                        enabled = false
                    ) {
                        Icon(
                            if (percentMatch >= 90) Icons.Filled.Mood else Icons.Filled.MoodBad,
                            "Match"
                        )
                    }

                    Button(
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(1F)
                            .size(64.dp)
                            .background(micColor)
                            .clip(CircleShape),
                        onClick = {
                            vm.textFromSpeech.value = ""
                            micColor = Color.Green
                            vm.startSpeechToText(context) {
                                micColor = Color.Blue
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            "Speak"
                        )
                    }

                    Button(
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(1F),
                        onClick = {
                            vm.textFromSpeech.value = ""
                            vm.selectRandomFromList()
                            vm.runTextToSpeech(context)
                        }
                    ) {
                        Icon(
                            Icons.Filled.SkipNext,
                            "Next"
                        )
                    }
                }
            }
        }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Icon(
                    modifier = Modifier.size(100.dp),
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Person"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = loadedPhrase,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        vm.runTextToSpeech(context)
                    }, enabled = isBtnEnabled,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.RecordVoiceOver,
                        "Listen voice"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = textFromSpeech,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

