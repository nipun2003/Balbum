package com.nipunapps.balbum.models

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.nipunapps.balbum.core.Constant

data class FileModel(
    val id : Long = 0L,
    val path: String = "",
    val timeStamp: String = "",
    val duration: Long? = null,
    val isSelected: Boolean = false,
    val lastModified: Long? = null,
    val size: Double? = null,
){
    val name = path.takeLastWhile { it != '/' }

    fun getMediaUri(mediaType : String) : Uri{
        return if(mediaType == Constant.IMAGE){
            ContentUris.withAppendedId(
                MediaStore.Images.Media.getContentUri("external"),
                id
            )
        }else{
            ContentUris.withAppendedId(
                MediaStore.Video.Media.getContentUri("external"),
                id
            )
        }
    }
}
