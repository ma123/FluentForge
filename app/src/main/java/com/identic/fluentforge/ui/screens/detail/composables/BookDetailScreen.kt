package com.identic.fluentforge.ui.screens.detail.composables

import android.app.DownloadManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.identic.fluentforge.MainActivity
import com.identic.fluentforge.R
import com.identic.fluentforge.dataReader.remote.utils.Utils
import com.identic.fluentforge.dataReader.remote.utils.book.BookUtils
import com.identic.fluentforge.dataReader.remote.utils.getActivity
import com.identic.fluentforge.ui.screens.commoncomposables.NetworkError
import com.identic.fluentforge.ui.screens.commoncomposables.ProgressDots
import com.identic.fluentforge.ui.screens.detail.viewmodels.BookDetailViewModel
import com.identic.fluentforge.ui.screens.viewmodels.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalCoilApi
@Composable
fun BookDetailScreen(
    bookId: String, navController: NavController
) {
    val viewModel: BookDetailViewModel = hiltViewModel()
    val state = viewModel.state

    val context = LocalContext.current
    val settingsVM = (context.getActivity() as MainActivity).settingsViewModel

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) { data ->
                Snackbar(
                    backgroundColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    snackbarData = data,
                )
            }
        },
        content = { paddingValues ->
            LaunchedEffect(key1 = true, block = {
                viewModel.getBookDetails(bookId)
            })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                BookDetailTopBar(onBackClicked = {
                    navController.navigateUp()
                })

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 65.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProgressDots()
                    }
                } else if (state.error != null) {
                    NetworkError(onRetryClicked = {
                        viewModel.getBookDetails(bookId)
                    })
                } else {
                    val book = state.bookSet.books.first()
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.background,
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background
                                            ), startY = 8f
                                        )
                                    )
                            )

                            Row(modifier = Modifier.fillMaxSize()) {

                                val imageUrl = state.extraInfo.coverImage.ifEmpty {
                                    book.formats.imagejpeg
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val imageBackground =
                                        if (settingsVM.getCurrentTheme() == ThemeMode.Dark) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                                        }
                                    Box(
                                        modifier = Modifier
                                            .shadow(24.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(imageBackground)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(imageUrl)
                                                .crossfade(true).build(),
                                            placeholder = painterResource(id = R.drawable.placeholder),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(118.dp)
                                                .height(169.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = book.title,
                                        modifier = Modifier
                                            .padding(
                                                start = 12.dp, end = 12.dp, top = 20.dp
                                            )
                                            .fillMaxWidth(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )

                                    Text(
                                        text = BookUtils.getAuthorsAsString(book.authors),
                                        modifier = Modifier.padding(
                                            start = 12.dp, end = 8.dp, top = 4.dp
                                        ),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )

                                    Spacer(modifier = Modifier.height(50.dp))
                                }
                            }
                        }

                        val pageCount = if (state.extraInfo.pageCount > 0) {
                            state.extraInfo.pageCount.toString()
                        } else {
                            stringResource(id = R.string.not_applicable)
                        }

                        // Check if this book is in downloadQueue.
                        val buttonTextValue =
                            if (viewModel.bookDownloader.isBookCurrentlyDownloading(book.id)) {
                                stringResource(id = R.string.cancel)
                            } else {
                                if (state.bookLibraryItem != null) stringResource(id = R.string.read_book_button) else stringResource(
                                    id = R.string.download_book_button
                                )
                            }

                        var buttonText by remember { mutableStateOf(buttonTextValue) }
                        var progressState by remember { mutableFloatStateOf(0f) }
                        var showProgressBar by remember { mutableStateOf(false) }

                        // Callable which updates book details screen button.
                        val updateBtnText: (Int?) -> Unit = { downloadStatus ->
                            buttonText = when (downloadStatus) {
                                DownloadManager.STATUS_RUNNING -> {
                                    showProgressBar = true
                                    context.getString(R.string.cancel)
                                }

                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    showProgressBar = false
                                    context.getString(R.string.read_book_button)
                                }

                                else -> {
                                    showProgressBar = false
                                    context.getString(R.string.download_book_button)
                                }
                            }
                        }

                        // Check if this book is in downloadQueue.
                        if (viewModel.bookDownloader.isBookCurrentlyDownloading(book.id)) {
                            progressState =
                                viewModel.bookDownloader.getRunningDownload(book.id)?.progress?.collectAsState()?.value!!
                            LaunchedEffect(key1 = progressState, block = {
                                updateBtnText(viewModel.bookDownloader.getRunningDownload(book.id)?.status)
                            })
                        }

                        MiddleBar(
                            bookLang = BookUtils.getLanguagesAsString(book.languages),
                            pageCount = pageCount,
                            downloadCount = Utils.prettyCount(book.downloadCount),
                            progressValue = progressState,
                            buttonText = buttonText,
                            showProgressBar = showProgressBar
                        ) {
                            when (buttonText) {
                                context.getString(R.string.read_book_button) -> {
                                    val bookLibraryItem = state.bookLibraryItem
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    /**
                                     *  Library item could be null if we reload the screen
                                     *  while some download was running, in that case we'll
                                     *  de-attach from our old state where download function
                                     *  will update library item and our new state will have
                                     *  no library item, i.e. null.
                                     */
                                    if (bookLibraryItem == null) {
                                        viewModel.viewModelScope.launch(Dispatchers.IO) {
                                            val libraryItem =
                                                viewModel.libraryDao.getItemById(book.id)!!
                                            withContext(Dispatchers.Main) {
                                                Utils.openBookFile(
                                                    context = context,
                                                    internalReader = viewModel.getInternalReaderSetting(),
                                                    libraryItem = libraryItem,
                                                    navController = navController
                                                )
                                            }
                                        }
                                    } else {
                                        Utils.openBookFile(
                                            context = context,
                                            internalReader = viewModel.getInternalReaderSetting(),
                                            libraryItem = bookLibraryItem,
                                            navController = navController
                                        )
                                    }

                                }

                                context.getString(R.string.download_book_button) -> {
                                    val message = viewModel.downloadBook(
                                        book, (context.getActivity() as MainActivity)
                                    ) { downloadProgress, downloadStatus ->
                                        progressState = downloadProgress
                                        updateBtnText(downloadStatus)
                                    }
                                    coroutineScope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(
                                            message = message,
                                        )
                                    }
                                }

                                context.getString(R.string.cancel) -> {
                                    viewModel.bookDownloader.cancelDownload(
                                        viewModel.bookDownloader.getRunningDownload(book.id)?.downloadId
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(id = R.string.book_synopsis),
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        val synopsis = state.extraInfo.description.ifEmpty { null }
                        if (synopsis != null) {
                            Text(
                                text = synopsis,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val compositionResult: LottieCompositionResult =
                                    rememberLottieComposition(
                                        spec = LottieCompositionSpec.RawRes(R.raw.synopis_not_found_lottie)
                                    )
                                val progressAnimation by animateLottieCompositionAsState(
                                    compositionResult.value,
                                    isPlaying = true,
                                    iterations = LottieConstants.IterateForever,
                                    speed = 1f
                                )

                                Spacer(modifier = Modifier.weight(2f))
                                LottieAnimation(
                                    composition = compositionResult.value,
                                    progress = progressAnimation,
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(200.dp),
                                    enableMergePaths = true
                                )

                                Text(
                                    text = stringResource(id = R.string.book_synopsis_not_found),
                                    modifier = Modifier.padding(14.dp),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        })
}

@ExperimentalMaterial3Api
@Composable
fun MiddleBar(
    bookLang: String,
    pageCount: String,
    downloadCount: String,
    progressValue: Float,
    buttonText: String,
    showProgressBar: Boolean,
    onButtonClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = progressValue,
        label = "download progress bar"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = showProgressBar) {
            if (progressValue > 0f) {
                // Determinate progress bar.
                LinearProgressIndicator(
                    progress = progress,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(start = 14.dp, end = 14.dp, top = 6.dp)
                        .clip(RoundedCornerShape(40.dp))
                )
            } else {
                // Indeterminate progress bar.
                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(start = 14.dp, end = 14.dp, top = 6.dp)
                        .clip(RoundedCornerShape(40.dp))
                )
            }
        }

        Card(
            modifier = Modifier
                .height(90.dp)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    2.dp
                )
            )
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.language),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, end = 4.dp)
                        )
                        Text(
                            text = bookLang,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, start = 4.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                }
                Divider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.book),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 13.dp, bottom = 15.dp, end = 4.dp)
                        )

                        Text(
                            text = pageCount,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, start = 4.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                Divider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.download),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 15.dp, bottom = 13.dp, end = 4.dp)
                        )
                        Text(
                            text = downloadCount,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, start = 4.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        Card(
            onClick = { onButtonClick() },
            modifier = Modifier
                .height(75.dp)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun BookDetailTopBar(
    onBackClicked: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .padding(16.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .clickable { onBackClicked() }) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.back),
                contentDescription = stringResource(id = R.string.back_button_desc),
                modifier = Modifier
                    .padding(14.dp)
                    .size(32.dp)
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun BookDetailScreenPreview() {
    BookDetailScreen(
        bookId = "0",
        navController = rememberNavController(),
    )
}