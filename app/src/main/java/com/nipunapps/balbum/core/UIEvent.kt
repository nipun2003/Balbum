package com.nipunapps.balbum.core

sealed class UIEvent {
    data class ShowSnackbar(val message: String): UIEvent()
}