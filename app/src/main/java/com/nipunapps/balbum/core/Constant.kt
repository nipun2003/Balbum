package com.nipunapps.balbum.core

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File


object Constant {
    const val IMAGE = "image"
    const val VIDEO = "video"

    const val DIRECTORY = "directory"
    const val INDEX = "index"
    const val ALL_VIDEOS = "All Videos"
    const val ALL_IMAGES = "All images"

    fun deleteFile(file : File,context: Context){
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName+".provider",
            file
        )
        val resolver = context.contentResolver
        resolver.delete(uri,null,null)
    }



    fun getFilePathToMediaID(songPath: String, context: Context): Long {
        var id: Long = 0
        val cr = context.contentResolver
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor: Cursor? = cr.query(uri, projection, "$selection=?", selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        return id
    }
}