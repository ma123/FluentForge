package com.identic.fluentforge.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.identic.fluentforge.R
import com.identic.fluentforge.domain.model.InternetRadio
import com.identic.fluentforge.reader.ui.common.BookItemCard
import com.identic.fluentforge.reader.ui.navigation.Screens
import com.identic.fluentforge.reader.ui.screens.home.composables.SearchAppBar
import com.identic.fluentforge.reader.ui.screens.home.viewmodels.UserAction
import com.identic.fluentforge.reader.utils.book.BookUtils
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
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Scaffold(
            bottomBar = {
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
                            if (radioScreenViewModel.isPlaying) Icons.Filled.PauseCircle
                            else Icons.Filled.PlayCircle
                        }, onUiEvent = radioScreenViewModel::onUIEvent)
                    }
                }
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
                                onRadioClick = { radioScreenViewModel.loadInternetRadio(radio) })
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
                        radioScreenViewModel.loadInternetRadios()
                    }
                }
            }
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
        Row(modifier = Modifier.fillMaxSize()) {
            val imageBackground = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(imageBackground)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(radio.favicon)
                        .crossfade(true).build(),
                    placeholder = painterResource(id = R.drawable.ic_book_downloads),
                    contentDescription = stringResource(id = R.string.title_radio),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
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
                text = "Retry"
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