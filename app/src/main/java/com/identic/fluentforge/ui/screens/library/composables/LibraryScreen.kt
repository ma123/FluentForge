package com.identic.fluentforge.ui.screens.library.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.ReadMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.identic.fluentforge.MainActivity
import com.identic.fluentforge.R
import com.identic.fluentforge.dataReader.remote.utils.Utils
import com.identic.fluentforge.dataReader.remote.utils.getActivity
import com.identic.fluentforge.dataReader.remote.utils.toToast
import com.identic.fluentforge.ui.navigation.Screens
import com.identic.fluentforge.ui.screens.detail.composables.BookDetailTopBar
import com.identic.fluentforge.ui.screens.library.viewmodels.LibraryViewModel
import com.identic.fluentforge.ui.screens.viewmodels.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox


@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun LibraryScreen(navController: NavController) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val state = viewModel.allItems.observeAsState(listOf()).value

    val context = LocalContext.current
    val settingsViewModel = (context.getActivity() as MainActivity).settingsViewModel

    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                BookDetailTopBar(onBackClicked = {
                    navController.navigateUp()
                })

                if (state.isEmpty()) {
                    NoLibraryItemAnimation()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        items(state.size) { i ->
                            val item = state[i]
                            if (item.fileExist()) {

                                val openDeleteDialog = remember { mutableStateOf(false) }

                                val detailsAction = SwipeAction(icon = {
                                    Image(
                                        modifier = Modifier.size(42.dp),
                                        imageVector = ImageVector.vectorResource(R.drawable.info),
                                        contentDescription = stringResource(id = R.string.info)
                                    )
                                }, background = MaterialTheme.colorScheme.primary, onSwipe = {
                                    viewModel.viewModelScope.launch {
                                        delay(250L)
                                        navController.navigate(
                                            Screens.BookDetailScreen.withBookId(
                                                item.bookId.toString()
                                            )
                                        )
                                    }
                                })

                                SwipeableActionsBox(
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                                    endActions = listOf(detailsAction),
                                    swipeThreshold = 85.dp
                                ) {
                                    LibraryCard(title = item.title,
                                        author = item.authors,
                                        item.getFileSize(),
                                        item.getDownloadDate(),
                                        onReadClick = {
                                            Utils.openBookFile(
                                                context = context,
                                                internalReader = viewModel.getInternalReaderSetting(),
                                                libraryItem = item,
                                                navController = navController
                                            )
                                        },
                                        onDeleteClick = { openDeleteDialog.value = true })
                                }

                                if (openDeleteDialog.value) {
                                    AlertDialog(onDismissRequest = {
                                        openDeleteDialog.value = false
                                    }, title = {
                                        Text(
                                            text = stringResource(id = R.string.library_delete_dialog_title),
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }, confirmButton = {
                                        TextButton(onClick = {
                                            openDeleteDialog.value = false
                                            val fileDeleted = item.deleteFile()
                                            if (fileDeleted) {
                                                viewModel.deleteItem(item)
                                            } else {
                                                context.getString(R.string.error).toToast(context)
                                            }
                                        }) {
                                            Text(stringResource(id = R.string.confirm))
                                        }
                                    }, dismissButton = {
                                        TextButton(onClick = {
                                            openDeleteDialog.value = false
                                        }) {
                                            Text(stringResource(id = R.string.cancel))
                                        }
                                    })
                                }

                            } else {
                                viewModel.deleteItem(item)
                            }
                        }
                    }

                    // Show tooltip for library screen.
                    LaunchedEffect(key1 = true) {
                        if (viewModel.shouldShowLibraryTooltip()) {
                            val result = snackBarHostState.showSnackbar(
                                message = context.getString(R.string.library_tooltip),
                                actionLabel = context.getString(R.string.got_it),
                                duration = SnackbarDuration.Indefinite
                            )

                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    viewModel.libraryTooltipDismissed()
                                }

                                SnackbarResult.Dismissed -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryCard(
    title: String,
    author: String,
    fileSize: String,
    date: String,
    onReadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            )
        ), shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .width(90.dp)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(64.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.library),
                    contentDescription = stringResource(id = R.string.title_library)
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = author,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Text(
                        text = fileSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Divider(
                        modifier = Modifier
                            .height(17.5.dp)
                            .width(1.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row {
                    LibraryCardButton(text = stringResource(id = R.string.library_read_button),
                        icon = Icons.Filled.ReadMore,
                        onClick = { onReadClick() })

                    Spacer(modifier = Modifier.width(10.dp))

                    LibraryCardButton(text = stringResource(id = R.string.library_delete_button),
                        icon = Icons.Filled.Delete,
                        onClick = { onDeleteClick() })
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun LibraryCardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(8.dp)
        )
        .clickable { onClick() }) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = 15.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
            )
        }
    }
}

@Composable
fun NoLibraryItemAnimation() {
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val compositionResult: LottieCompositionResult = rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(R.raw.no_library_items_lottie)
        )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
            speed = 1f
        )

        Spacer(modifier = Modifier.weight(1f))
        LottieAnimation(
            composition = compositionResult.value,
            progress = progressAnimation,
            modifier = Modifier.size(300.dp),
            enableMergePaths = true
        )

        Text(
            text = stringResource(id = R.string.empty_library),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp)
        )
        Spacer(modifier = Modifier.weight(1.4f))
    }
}


@ExperimentalMaterial3Api
@Composable
@Preview
fun LibraryScreenPreview() {
    LibraryCard(title = "The Idiot",
        author = "Fyodor Dostoevsky",
        fileSize = "5.9MB",
        date = "01- Jan -2020",
        onReadClick = {},
        onDeleteClick = {})
}
