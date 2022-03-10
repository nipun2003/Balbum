package com.nipunapps.balbum.models

data class FileModel(
    val path : String = "",
){
    val name = path.takeLastWhile { it != '/' }
}
