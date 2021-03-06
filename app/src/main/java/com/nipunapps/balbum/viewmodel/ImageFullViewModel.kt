package com.nipunapps.balbum.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.UIEvent
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel
import com.nipunapps.balbum.repository.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class ImageFullViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _index = mutableStateOf(0)
    val index: State<Int> = _index

    private val _directoryModel = mutableStateOf(DirectoryModel())
    val directoryModel: State<DirectoryModel> = _directoryModel

    private val _items = mutableStateOf(emptyList<FileModel>())
    val items: State<List<FileModel>> = _items

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<String>(Constant.DIRECTORY)?.let { d ->
            _index.value = savedStateHandle.get<Int>(Constant.INDEX) ?: 0
            _directoryModel.value =
                Json.decodeFromString<DirectoryModel>(URLDecoder.decode(d, "utf-8"))
            _items.value = directoryRepository.getData(directoryModel.value)
        }
    }

    fun reloadData() {
        if (index.value == items.value.size - 1) _index.value = index.value - 1
        _items.value = directoryRepository.getData(directoryModel.value)
    }

    fun changeIndex(index: Int) {
        _index.value = index
    }

    fun sendFile() {
        val file = items.value[index.value]
        directoryRepository.sendFile(file.path, directoryModel.value.mediaType)
    }

    fun deleteItems(): List<Uri> {
        val item = index.value
        val file = items.value[item]
        return listOf(file.getMediaUri(directoryModel.value.mediaType))
    }

    fun playVideo() {
        val path = items.value[index.value].path
        directoryRepository.playVideo(path = path)
    }
}