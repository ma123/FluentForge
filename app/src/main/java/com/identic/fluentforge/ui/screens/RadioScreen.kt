package com.identic.fluentforge.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.identic.fluentforge.R
import com.identic.fluentforge.domain.model.InternetRadio
import com.identic.fluentforge.ui.theme.LightSurface
import com.identic.fluentforge.ui.viewmodel.RadioScreenViewModel
import com.identic.fluentforge.ui.viewmodel.UIEvent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RadioScreen(
    radioScreenViewModel: RadioScreenViewModel = hiltViewModel(),
    startService: () -> Unit
) {
    val radioList by remember { radioScreenViewModel.radiosList }
    val loadError by remember { radioScreenViewModel.loadError }
    val isLoading by remember { radioScreenViewModel.isLoading }
    val pullRefreshState =
        rememberPullRefreshState(isLoading, { radioScreenViewModel.loadInternetRadios() })

    val state = radioScreenViewModel.uiState.collectAsStateWithLifecycle()
    Surface(modifier = Modifier
        .fillMaxSize()) {

        Scaffold(
            topBar = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.listen_and_learn),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

            },
            bottomBar = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LaunchedEffect(true) { // This is only call first time
                        startService()
                    }

                    PlayerControls(playResourceProvider = {
                        if (radioScreenViewModel.isPlaying) R.drawable.ic_baseline_pause_24
                        else R.drawable.ic_baseline_play_arrow_24
                    }, onUiEvent = radioScreenViewModel::onUIEvent)
                }
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 60.dp)
                        .align(Alignment.TopCenter)
                ) {
                    items(items = radioList,
                        key = {
                            it.id
                        }) { radio ->
                        RadioItem(
                            radio,
                            onRadioClick = { radioScreenViewModel.loadInternetRadio(radio) })
                    }
                }
                PullRefreshIndicator(
                    isLoading,
                    pullRefreshState,
                    Modifier.align(Alignment.TopCenter)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                if (loadError.isNotEmpty()) {
                    RetrySection(error = loadError) {
                        radioScreenViewModel.loadInternetRadios()
                    }
                }
            }
        }
    }
}

@Composable
fun RadioItem(radio: InternetRadio, onRadioClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(all = 5.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onRadioClick)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(radio.favicon)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_foreground)
                .build(),
            contentDescription = radio.name,
            onSuccess = {
            },
            modifier = Modifier
                .size(64.dp)
                .padding(start = 5.dp)
                .align(Alignment.CenterVertically),
            loading = {

                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(0.5f)
                )
            }
        )

        Column(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .weight(weight = 1f)
        ) {
            Text(
                text = radio.name!!,
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp)
            )

            Text(
                text = radio.tags!!,
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp)
            )
        }
    }
}

@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column {
        Text(
            text = error,
            color = Color.Red,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onRetry() },
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Text(
                text = "Retry"
            )
        }
    }
}

@Composable
fun PlayerControls(
    playResourceProvider: () -> Int,
    onUiEvent: (UIEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = playResourceProvider()),
            contentDescription = "Play/Pause Button",
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = { onUiEvent(UIEvent.PlayPause) })
                .padding(8.dp)
                .size(56.dp)
        )
    }
}