package com.identic.fluentforge.ui.screens.home.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.identic.fluentforge.R
import com.identic.fluentforge.common.Constants
import com.identic.fluentforge.dataReader.remote.utils.NetworkObserver
import com.identic.fluentforge.dataReader.remote.utils.book.BookUtils
import com.identic.fluentforge.ui.screens.commoncomposables.BookItemCard
import com.identic.fluentforge.ui.screens.commoncomposables.NetworkError
import com.identic.fluentforge.ui.screens.commoncomposables.ProgressDots
import com.identic.fluentforge.ui.navigation.Screens
import com.identic.fluentforge.ui.screens.home.viewmodels.HomeViewModel
import com.identic.fluentforge.ui.screens.home.viewmodels.UserAction

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun HomeScreen(navController: NavController, networkStatus: NetworkObserver.Status) {

    val viewModel: HomeViewModel = hiltViewModel()

    /*
     Block back button press if search bar is visible to avoid
     app from closing immediately, instead disable search bar
     on first back press, and close app on second.
     */
    val sysBackButtonState = remember { mutableStateOf(false) }
    BackHandler(enabled = sysBackButtonState.value) {
        if (viewModel.topBarState.searchText.isNotEmpty()) {
            viewModel.onAction(UserAction.TextFieldInput("", networkStatus))
        }
    }

    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetElevation = 24.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        sheetContent = {
        }) {
        HomeScreenScaffold(
            viewModel = viewModel,
            networkStatus = networkStatus,
            navController = navController,
            sysBackButtonState = sysBackButtonState
        )
    }
}


@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun HomeScreenScaffold(
    viewModel: HomeViewModel,
    networkStatus: NetworkObserver.Status,
    navController: NavController,
    sysBackButtonState: MutableState<Boolean>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val allBooksState = viewModel.allBooksState
    val topBarState = viewModel.topBarState

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
            ) {
                SearchAppBar(onInputValueChange = { newText ->
                    viewModel.onAction(
                        UserAction.TextFieldInput(newText, networkStatus)
                    )
                }, text = topBarState.searchText, onSearchClicked = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
                sysBackButtonState.value = true
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    navController.navigate(Screens.LibraryScreen.route)
                },
                shape = CircleShape,
            ) {
                Image(
                    modifier = Modifier.size(64.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.library),
                    contentDescription = stringResource(id = R.string.title_library)
                )
            }

        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                // If search text is empty show list of all books.
                if (topBarState.searchText.isBlank()) {
                    // show fullscreen progress indicator when loading the first page.
                    if (allBooksState.page == 1L && allBooksState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (!allBooksState.isLoading && allBooksState.error != null) {
                        NetworkError(onRetryClicked = { viewModel.reloadItems() })
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(start = 8.dp, end = 8.dp),
                            columns = GridCells.Adaptive(295.dp)
                        ) {
                            items(allBooksState.items.size) { i ->
                                val item = allBooksState.items[i]
                                if (networkStatus == NetworkObserver.Status.Available
                                    && i >= allBooksState.items.size - 1
                                    && !allBooksState.endReached
                                    && !allBooksState.isLoading
                                ) {
                                    viewModel.loadNextItems()
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BookItemCard(
                                        title = item.title,
                                        author = BookUtils.getAuthorsAsString(item.authors),
                                        language = BookUtils.getLanguagesAsString(item.languages),
                                        subjects = BookUtils.getSubjectsAsString(
                                            item.subjects, 3
                                        ),
                                        coverImageUrl = item.formats.imagejpeg
                                    ) {
                                        navController.navigate(
                                            Screens.BookDetailScreen.withBookId(
                                                item.id.toString()
                                            )
                                        )
                                    }
                                }

                            }
                            item {
                                if (allBooksState.isLoading) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        ProgressDots()
                                    }
                                }
                            }
                        }
                    }

                    // Else show the search results.
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(start = 8.dp, end = 8.dp),
                        columns = GridCells.Adaptive(295.dp)
                    ) {
                        if (topBarState.isSearching) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ProgressDots()
                                }
                            }
                        }

                        items(topBarState.searchResults.size) { i ->
                            val item = topBarState.searchResults[i]
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                BookItemCard(
                                    title = item.title,
                                    author = BookUtils.getAuthorsAsString(item.authors),
                                    language = BookUtils.getLanguagesAsString(item.languages),
                                    subjects = BookUtils.getSubjectsAsString(
                                        item.subjects, 3
                                    ),
                                    coverImageUrl = item.formats.imagejpeg
                                ) {
                                    navController.navigate(
                                        Screens.BookDetailScreen.withBookId(
                                            item.id.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SearchAppBar(
    onInputValueChange: (String) -> Unit,
    text: String,
    onSearchClicked: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = text,
        onValueChange = {
            onInputValueChange(it)
        },
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp
        ),
        placeholder = {
            Text(
                text = "Search...",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.medium)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = ContentAlpha.medium
                )
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                if (text.isNotEmpty()) {
                    onInputValueChange("")
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Icon",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = ContentAlpha.medium
            ),
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
        shape = RoundedCornerShape(16.dp)
    )
}