package com.identic.fluentforge.reader.ui.screens.library.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identic.fluentforge.reader.database.library.LibraryDao
import com.identic.fluentforge.reader.database.library.LibraryItem
import com.identic.fluentforge.reader.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    val allItems: LiveData<List<LibraryItem>> = libraryDao.getAllItems()

    fun deleteItem(item: LibraryItem) {
        viewModelScope.launch(Dispatchers.IO) { libraryDao.delete(item) }
    }

    fun getInternalReaderSetting() = preferenceUtil.getBoolean(
        PreferenceUtil.INTERNAL_READER_BOOL, true
    )

    fun shouldShowLibraryTooltip() = preferenceUtil.getBoolean(
        PreferenceUtil.SHOW_LIBRARY_TOOLTIP_BOOL, true
    )

    fun libraryTooltipDismissed() = preferenceUtil.putBoolean(
        PreferenceUtil.SHOW_LIBRARY_TOOLTIP_BOOL, false
    )
}