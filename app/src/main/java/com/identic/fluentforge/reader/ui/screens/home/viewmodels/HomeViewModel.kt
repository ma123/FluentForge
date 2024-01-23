package com.identic.fluentforge.reader.ui.screens.home.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identic.fluentforge.reader.repo.BookRepository
import com.identic.fluentforge.reader.repo.models.Book
import com.identic.fluentforge.reader.repo.models.BookSet
import com.identic.fluentforge.reader.utils.NetworkObserver
import com.identic.fluentforge.reader.utils.Paginator
import com.identic.fluentforge.reader.utils.PreferenceUtil
import com.identic.fluentforge.reader.utils.book.BookLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllBooksState(
    val isLoading: Boolean = false,
    val items: List<Book> = emptyList(),
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Long = 1L
)

data class TopBarState(
    val searchText: String = "",
    val isSortMenuVisible: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<Book> = emptyList()
)

sealed class UserAction {
    data class TextFieldInput(
        val text: String,
        val networkStatus: NetworkObserver.Status
    ) : UserAction()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    var allBooksState by mutableStateOf(AllBooksState())
    var topBarState by mutableStateOf(TopBarState())

    private val _language: MutableState<BookLanguage> = mutableStateOf(getPreferredLanguage())
    val language: State<BookLanguage> = _language

    private var searchJob: Job? = null

    private val pagination = Paginator(initialPage = allBooksState.page, onLoadUpdated = {
        allBooksState = allBooksState.copy(isLoading = it)
    }, onRequest = { nextPage ->
        try {
            bookRepository.getAllBooks(nextPage, language.value)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }, getNextPage = {
        allBooksState.page + 1L
    }, onError = {
        allBooksState = allBooksState.copy(error = it?.localizedMessage ?: "unknown-error")
    }, onSuccess = { bookSet, newPage ->
        /**
         * usually bookSet.books is not nullable and API simply returns empty list
         * when browsing books all books (i.e. without passing language parameter)
         * however, when browsing by language it returns a response which looks like
         * this: {"detail": "Invalid page."}. Hence the [BookSet] attributes become
         * null in this case and can cause crashes.
         */
        /**
         * usually bookSet.books is not nullable and API simply returns empty list
         * when browsing books all books (i.e. without passing language parameter)
         * however, when browsing by language it returns a response which looks like
         * this: {"detail": "Invalid page."}. Hence the [BookSet] attributes become
         * null in this case and can cause crashes.
         */
        @Suppress("SENSELESS_COMPARISON") val books = if (bookSet.books != null) {
            bookSet.books.filter { it.formats.applicationepubzip != null } as ArrayList<Book>
        } else {
            ArrayList()
        }

        allBooksState = allBooksState.copy(
            items = (allBooksState.items + books),
            page = newPage,
            endReached = books.isEmpty()
        )
    })

    init {
        loadNextItems()
    }

    fun loadNextItems() {
        viewModelScope.launch {
            pagination.loadNextItems()
        }
    }

    fun reloadItems() {
        pagination.reset()
        allBooksState = AllBooksState()
        loadNextItems()
    }

    fun onAction(userAction: UserAction) {
        when (userAction) {
            is UserAction.TextFieldInput -> {
                topBarState = topBarState.copy(searchText = userAction.text)
                if (userAction.networkStatus == NetworkObserver.Status.Available) {
                    searchJob?.cancel()
                    searchJob = viewModelScope.launch {
                        if (userAction.text.isNotBlank()) {
                            topBarState = topBarState.copy(isSearching = true)
                        }
                        delay(500L)
                        searchBooks(userAction.text)
                    }
                }
            }
        }
    }

    private suspend fun searchBooks(query: String) {
        val bookSet = bookRepository.searchBooks(query)
        val books = bookSet.getOrNull()!!.books.filter { it.formats.applicationepubzip != null }
        topBarState = topBarState.copy(searchResults = books, isSearching = false)
    }

    private fun getPreferredLanguage(): BookLanguage {
        val isoCode = preferenceUtil.getString(
            PreferenceUtil.PREFERRED_BOOK_LANG_STR,
            BookLanguage.English.isoCode
        )
        return BookLanguage.getAllLanguages().find { it.isoCode == isoCode }
            ?: BookLanguage.AllBooks
    }
}