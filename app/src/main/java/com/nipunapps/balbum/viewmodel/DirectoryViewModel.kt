package com.nipunapps.balbum.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.UIEvent
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel
import com.nipunapps.balbum.repository.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _directoryModel = mutableStateOf(DirectoryModel())
    val directoryModel: State<DirectoryModel> = _directoryModel

    private val _items = mutableStateOf(emptyList<FileModel>())
    val items: State<List<FileModel>> = _items

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<String>(Constant.DIRECTORY)?.let { d ->
            _directoryModel.value =
                Json.decodeFromString<DirectoryModel>(URLDecoder.decode(d, "utf-8"))
            _items.value = directoryRepository.getData(directoryModel.value)
        }
    }

    fun updateItems() {
        _items.value = directoryRepository.getData(directoryModel.value)
    }

    fun toggleSelection(
        index: Int
    ) {
        val list = items.value
        _items.value = list.mapIndexed { i, fileModel ->
            if (i == index) {
                fileModel.copy(
                    path = fileModel.path,
                    isSelected = !fileModel.isSelected,
                )
            } else fileModel
        }
    }

    fun toggleAllSelect(value: Boolean) {
        val list = items.value
        _items.value = list.map { fileModel ->
            fileModel.copy(
                path = fileModel.path,
                isSelected = value
            )
        }
    }

    fun deleteItems(): List<Uri> {
        return items.value.filter { it.isSelected }.map { it.getMediaUri(directoryModel.value.mediaType) }
    }

    fun sendFiles() {
        val selectedItems = items.value.filter { it.isSelected }.map { it.path }
        viewModelScope.launch {
            if (selectedItems.size > 30) {
                _eventFlow.emit(
                    UIEvent.ShowSnackbar(
                        message = "Can't send files more than 30"
                    )
                )
            } else {
                directoryRepository.sendMultipleFiles(selectedItems, directoryModel.value.mediaType)
            }
        }
    }
}