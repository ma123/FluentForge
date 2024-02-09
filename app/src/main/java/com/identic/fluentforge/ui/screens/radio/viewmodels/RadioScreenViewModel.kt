package com.identic.fluentforge.ui.screens.radio.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.identic.fluentforge.common.Resource
import com.identic.fluentforge.dataRadio.remote.repository.InternetRadioRepository
import com.identic.fluentforge.domain.model.InternetRadio
import com.identic.fluentforge.service.PlayerEvent
import com.identic.fluentforge.service.SimpleMediaServiceHandler
import com.identic.fluentforge.service.SimpleMediaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class RadioScreenViewModel @Inject constructor(
    private val repository: InternetRadioRepository,
    private val simpleMediaServiceHandler: SimpleMediaServiceHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var radiosList = mutableStateOf<List<InternetRadio>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    @OptIn(SavedStateHandleSaveableApi::class)
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)

    init {
        viewModelScope.launch {
            loadInternetRadios()

            simpleMediaServiceHandler.simpleMediaState.collect { mediaState ->
                when (mediaState) {
                    is SimpleMediaState.Playing -> isPlaying = mediaState.isPlaying
                    is SimpleMediaState.Ready -> {
                        _uiState.value = UIState.Ready
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.Stop)
        }
    }

    fun onUIEvent(uiEvent: UIEvent) = viewModelScope.launch {
        when (uiEvent) {
            UIEvent.PlayPause -> simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.PlayPause)
        }
    }

    fun loadInternetRadios() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository
                    .getAllInternetRadios()
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { radios ->
                                    radiosList.value = radios
                                }
                                loadError.value = ""
                                isLoading.value = false
                            }

                            is Resource.Error -> {
                                loadError.value = result.message!!
                                isLoading.value = false
                            }

                            is Resource.Loading -> {
                                withContext(Dispatchers.Main) {
                                    isLoading.value = true
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadInternetRadio(item: InternetRadio) {
        val mediaItem = MediaItem.Builder()
            .setUri(item.urlResolved)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                    .setArtworkUri(Uri.parse(item.favicon))
                    .setAlbumTitle(item.name)
                    .setDisplayTitle(item.tags)
                    .build()
            ).build()

        simpleMediaServiceHandler.addMediaItem(mediaItem)
        onUIEvent(UIEvent.PlayPause)
    }
}

sealed class UIEvent {
    object PlayPause : UIEvent()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}
