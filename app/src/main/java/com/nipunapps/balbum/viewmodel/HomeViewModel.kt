package com.nipunapps.balbum.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.Resource
import com.nipunapps.balbum.core.UIEvent
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.storage.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
                    path = directoryModel.path,
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
                path = directoryModel.path,
                isSelected = value,
                mediaType = directoryModel.mediaType
            ).apply {
                firstFilePath = directoryModel.firstFilePath
                count = directoryModel.count
            }
        }
    }

    fun updateItems(){
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

    fun deleteItems() {
        storageRepository.deleteDirectory(directoryState.value.filter { it.isSelected })
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {

                    }
                    is Resource.Error -> {
                        _eventFlow.emit(
                            UIEvent.ShowSnackbar(
                                result.message ?: "Error deleting file"
                            )
                        )
                    }
                    is Resource.Success -> {
                        _directoryState.value = result.data ?: directoryState.value
                    }
                }
            }.launchIn(viewModelScope)
    }
}