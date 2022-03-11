package com.nipunapps.balbum.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.UIEvent
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val _directoryState = mutableStateOf(emptyList<DirectoryModel>())
    val directoryState: State<List<DirectoryModel>> = _directoryState

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        _directoryState.value = storageRepository.getAllDirectory()
    }

    fun toggleSelection(
        index: Int
    ) {
        val list = directoryState.value
        _directoryState.value = list.mapIndexed { i, directoryModel ->
            if (i == index) {
                directoryModel.copy(
                    name = directoryModel.name,
                    isSelected = !directoryModel.isSelected,
                    mediaType = directoryModel.mediaType
                ).apply {
                    firstFilePath = directoryModel.firstFilePath
                    count = directoryModel.count
                }
            } else directoryModel
        }
    }

    fun toggleAllSelect(value: Boolean) {
        val list = directoryState.value
        _directoryState.value = list.map { directoryModel ->
            directoryModel.copy(
                name = directoryModel.name,
                isSelected = value,
                mediaType = directoryModel.mediaType
            ).apply {
                firstFilePath = directoryModel.firstFilePath
                count = directoryModel.count
            }
        }
    }

    fun updateItems() {
        _directoryState.value = storageRepository.getAllDirectory()
    }

    fun getLastImageIndex(): Int {
        directoryState.value.forEachIndexed { index, directoryModel ->
            if (directoryModel.mediaType == Constant.VIDEO) return index
        }
        return directoryState.value.size
    }

    fun shareFiles() {
        val shareItem = directoryState.value.filter { it.isSelected }[0].count
        viewModelScope.launch {
            if (shareItem > 30) {
                _eventFlow.emit(
                    UIEvent.ShowSnackbar(
                        message = "Can't send files more than 30"
                    )
                )
            } else {
                storageRepository.sendMultipleFiles(directoryState.value.filter { it.isSelected })
            }
        }
    }

    fun deleteItems(): List<Uri> {
        return storageRepository.deleteDirectory(directoryState.value.filter { it.isSelected })
    }
}