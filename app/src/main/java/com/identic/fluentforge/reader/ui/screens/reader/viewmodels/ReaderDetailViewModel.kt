package com.identic.fluentforge.reader.ui.screens.reader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identic.fluentforge.reader.database.library.LibraryDao
import com.identic.fluentforge.reader.database.reader.ReaderDao
import com.identic.fluentforge.reader.database.reader.ReaderItem
import com.identic.fluentforge.reader.epub.createEpubBook
import com.identic.fluentforge.reader.epub.models.EpubBook
import com.identic.fluentforge.reader.repo.BookRepository
import com.identic.fluentforge.reader.utils.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import javax.inject.Inject


data class EbookData(
    val coverImage: String?,
    val title: String,
    val author: String,
    val epubBook: EpubBook,
)

data class ReaderDetailScreenState(
    val isLoading: Boolean = true,
    val ebookData: EbookData? = null,
    val error: String? = null,
    val readerItem: ReaderItem? = null
)

@HiltViewModel
class ReaderDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val libraryDao: LibraryDao,
    private val readerDao: ReaderDao
) : ViewModel() {

    companion object Errors {
        const val FILE_NOT_FOUND = "epub_file_not_found"
    }

    var state by mutableStateOf(ReaderDetailScreenState())
    fun loadEbookData(bookId: String, networkStatus: NetworkObserver.Status) {
        viewModelScope.launch(Dispatchers.IO) {
            // build EbookData.
            val libraryItem = libraryDao.getItemById(bookId.toInt())!!
            state = try {
                val coverImage: String? = try {
                    if (networkStatus == NetworkObserver.Status.Available) bookRepository.getExtraInfo(
                        libraryItem.title
                    )?.coverImage else null
                } catch (exc: Exception) {
                    null
                }
                state.copy(
                    isLoading = false, ebookData = EbookData(
                        coverImage,
                        libraryItem.title,
                        libraryItem.authors,
                        createEpubBook(libraryItem.filePath)
                    ), readerItem = readerDao.getReaderItem(bookId.toInt())
                )

            } catch (exc: FileNotFoundException) {
                state.copy(isLoading = false, error = FILE_NOT_FOUND)
            }
        }
    }
}