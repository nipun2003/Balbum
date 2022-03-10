package com.nipunapps.balbum.models

data class DirectoryItemModel(
    val time : String = "",
    val items : List<FileModel> = emptyList()
)