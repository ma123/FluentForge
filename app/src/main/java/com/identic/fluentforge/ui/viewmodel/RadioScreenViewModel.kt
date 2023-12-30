package com.identic.fluentforge.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.identic.fluentforge.common.Resource
import com.identic.fluentforge.data.remote.repository.InternetRadioRepository
import com.identic.fluentforge.domain.model.InternetRadio
import com.identic.fluentforge.service.PlayerEvent
import com.identic.fluentforge.service.SimpleMediaServiceHandler
import com.identic.fluentforge.service.SimpleMediaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = _uiState.asStateFlow()

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
            else -> {}
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

        //val mediaItemList = mutableListOf<MediaItem>()
        //(1..17).forEach {
        //    mediaItemList.add(
        //        MediaItem.Builder()
        //            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$it.mp3")
        //            .setMediaMetadata(MediaMetadata.Builder()
        //                .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
        //                .setArtworkUri(Uri.parse("https://cdns-images.dzcdn.net/images/cover/1fddc1ab0535ee34189dc4c9f5f87bf9/264x264.jpg"))
        //                .setAlbumTitle("SoundHelix")
        //                .setDisplayTitle("Song $it")
        //                .build()
        //            ).build()
        //    )
        //}

        simpleMediaServiceHandler.addMediaItem(mediaItem)
        //simpleMediaServiceHandler.addMediaItemList(mediaItemList)
        onUIEvent(UIEvent.PlayPause)
    }
}

sealed class UIEvent {
    object PlayPause : UIEvent()
    object Backward : UIEvent()
    object Forward : UIEvent()
    data class UpdateProgress(val newProgress: Float) : UIEvent()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}
