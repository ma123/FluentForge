package com.identic.fluentforge.ui.screens.radio.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.identic.fluentforge.R
import com.identic.fluentforge.domain.model.InternetRadio
import com.identic.fluentforge.ui.screens.radio.viewmodels.RadioScreenViewModel
import com.identic.fluentforge.ui.screens.radio.viewmodels.UIEvent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RadioScreen(
    startService: () -> Unit
) {
    val vm: RadioScreenViewModel = hiltViewModel()
    val radioList by remember { vm.radiosList }
    val loadError by remember { vm.loadError }
    val isLoading by remember { vm.isLoading }
    val pullRefreshState =
        rememberPullRefreshState(isLoading, { vm.loadInternetRadios() })

    Surface(
        modifier = Modifier
            .fillMaxSize().padding(bottom = 70.dp)
    ) {
        Scaffold(
            bottomBar = {
                InternetRadioBottomBar(vm, startService)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {

                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 8.dp, end = 8.dp),
                    columns = GridCells.Adaptive(295.dp)
                ) {
                    items(items = radioList,
                        key = {
                            it.id
                        }) { radio ->

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            RadioItem(
                                radio,
                                onRadioClick = { vm.loadInternetRadio(radio) })
                        }
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
                        vm.loadInternetRadios()
                    }
                }
            }
        }
    }
}

@Composable
fun InternetRadioBottomBar(vm: RadioScreenViewModel, startService: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            LaunchedEffect(true) { // This is only call first time
                startService()
            }

            PlayerControls(playResourceProvider = {
                if (vm.isPlaying) Icons.Filled.PauseCircle
                else Icons.Filled.PlayCircle
            }, onUiEvent = vm::onUIEvent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioItem(radio: InternetRadio, onRadioClick: () -> Unit) {

    Card(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        onClick = onRadioClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val imageBackground = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(imageBackground)
                    .align(Alignment.CenterVertically)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(radio.favicon)
                        .crossfade(true).build(),
                    contentDescription = stringResource(id = R.string.title_radio),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Filled.Radio),
                    alignment = Alignment.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = radio.name!!,
                    modifier = Modifier
                        .padding(
                            start = 12.dp, end = 8.dp
                        )
                        .fillMaxWidth(),
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = radio.tags!!,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.weight(1f))
            }
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
                text = stringResource(id = R.string.retry)
            )
        }
    }
}

@Composable
fun PlayerControls(
    playResourceProvider: () -> ImageVector,
    onUiEvent: (UIEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            imageVector = playResourceProvider(),
            contentDescription = "Play/Pause Button",
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = { onUiEvent(UIEvent.PlayPause) })
                .padding(8.dp)
                .size(56.dp)
        )
    }
}