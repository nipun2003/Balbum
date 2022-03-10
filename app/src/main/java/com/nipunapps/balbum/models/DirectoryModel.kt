package com.nipunapps.balbum.models

import com.nipunapps.balbum.core.Constant.IMAGE

data class DirectoryModel(
    val path : String = "/All Photos/",
    val mediaType : String = IMAGE,
    val isSelected : Boolean = false
){
    private val name : String = path.dropLast(1).takeLastWhile { it != '/' }
    var count = 1
    var firstFilePath = ""

    fun getName() : String {
        return if(name == "0") "Phone Storage" else name
    }
}
