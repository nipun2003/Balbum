package com.nipunapps.balbum.models

import android.os.Parcelable
import com.nipunapps.balbum.core.Constant.IMAGE
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DirectoryModel(
    val name : String = "",
    val mediaType : String = "",
    val bucketId : String = "",
    val isSelected : Boolean = false,
) : Parcelable {
    var count = 1
    var firstFilePath = ""

    fun getFilterName() : String {
        return name
    }
}
