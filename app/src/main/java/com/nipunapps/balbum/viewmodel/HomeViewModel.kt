package com.nipunapps.balbum.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.storage.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val _directoryState = mutableStateOf(emptyList<DirectoryModel>())
    val directoryState : State<List<DirectoryModel>> = _directoryState

    init {
        _directoryState.value = storageRepository.getAllDirectory()
    }
}