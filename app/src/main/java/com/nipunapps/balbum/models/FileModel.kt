package com.nipunapps.balbum.models

data class FileModel(
    val path : String = "",
    val timeStamp : String = "",
    val duration : Long? = null,
    val isSelected : Boolean = false
){
    val name = path.takeLastWhile { it != '/' }
}
