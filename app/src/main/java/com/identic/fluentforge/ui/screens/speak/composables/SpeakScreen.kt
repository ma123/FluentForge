package com.identic.fluentforge.ui.screens.speak.composables

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.identic.fluentforge.R
import com.identic.fluentforge.common.Constants.PERCENT_MATCH
import com.identic.fluentforge.ui.screens.speak.viewmodels.SpeakScreenViewModel
import timber.log.Timber

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SpeakScreen() {
    val vm: SpeakScreenViewModel = hiltViewModel()
    val context = LocalContext.current

    val loadedPhrase by remember { vm.loadedPhrase }
    val isBtnEnabled by remember { vm.isBtnEnabled }
    val textFromSpeech by remember { vm.textFromSpeech }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
    ) {
        Scaffold(bottomBar = {
            SpeakBottomBar(
                vm = vm,
                textFromSpeech = textFromSpeech,
                loadedPhrase = loadedPhrase,
                context = context
            )
        }) {
            SpeakContent(
                vm = vm,
                textFromSpeech = textFromSpeech,
                loadedPhrase = loadedPhrase,
                isBtnEnabled = isBtnEnabled,
                context = context
            )
        }
    }
}

@Composable
fun SpeakContent(
    vm: SpeakScreenViewModel,
    textFromSpeech: String,
    loadedPhrase: String,
    isBtnEnabled: Boolean,
    context: Context
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            modifier = Modifier.size(120.dp),
            imageVector = ImageVector.vectorResource(R.drawable.listening_user),
            contentDescription = stringResource(id = R.string.person)
        )

        Text(
            text = loadedPhrase,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        FilledIconButton(
            onClick = {
                Timber.d("### i am here")
                vm.runTextToSpeech(context)
            }, enabled = isBtnEnabled,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(64.dp),
            shape = CircleShape,
            content = {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.headphones),
                    contentDescription = stringResource(id = R.string.listen_voice)
                )
            }
        )

        Text(
            text = textFromSpeech,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}

@Composable
fun SpeakBottomBar(
    vm: SpeakScreenViewModel,
    textFromSpeech: String,
    loadedPhrase: String,
    context: Context
) {
    val percentMatch by remember { vm.percentMatch }
    var isRunning by remember { mutableStateOf(false) }

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
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                modifier = Modifier
                    .size(64.dp),
                imageVector = if (percentMatch == 0) {
                    ImageVector.vectorResource(R.drawable.neutral_answer)
                } else {
                    if (percentMatch >= PERCENT_MATCH)
                        ImageVector.vectorResource(R.drawable.good_answer)
                    else
                        ImageVector.vectorResource(R.drawable.bad_answer)
                },
                contentDescription = stringResource(id = R.string.match)
            )

            val infiniteTransition = rememberInfiniteTransition(label = "")

            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRunning) 1.2f else 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            )

            FilledIconButton(
                modifier = Modifier
                    .size(82.dp)
                    .scale(scale),
                shape = CircleShape,
                onClick = {
                    vm.percentMatch.intValue = 0
                    vm.textFromSpeech.value = ""
                    isRunning = true
                    vm.startSpeechToText(context) {
                        isRunning = false
                    }
                },
                content = {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.speak),
                        contentDescription = stringResource(id = R.string.speak)
                    )
                }
            )

            FilledIconButton(
                modifier = Modifier
                    .size(64.dp),
                shape = CircleShape,
                onClick = {
                    vm.percentMatch.intValue = 0
                    vm.textFromSpeech.value = ""
                    vm.selectRandomFromList()
                    vm.runTextToSpeech(context)
                },
                content = {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.next),
                        contentDescription = stringResource(id = R.string.next)
                    )
                }
            )
        }
    }
}