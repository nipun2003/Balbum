package com.nipunapps.balbum.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.models.DirectoryItemModel
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel
import com.nipunapps.balbum.storage.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val directoryModel : State<DirectoryModel> = _directoryModel

    private val _items = mutableStateOf(DirectoryItemModel())
    val items : State<DirectoryItemModel> = _items

    init {
        savedStateHandle.get<String>(Constant.DIRECTORY)?.let { d->
            _directoryModel.value = Json.decodeFromString<DirectoryModel>(URLDecoder.decode(d,"utf-8"))
            _items.value = DirectoryItemModel(items = directoryRepository.getData(directoryModel.value))
        }
    }
}