package com.nipunapps.balbum.storage

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel

class DirectoryRepository(
    private val context: Context
) {

    fun getData(directoryModel: DirectoryModel): List<FileModel> {
        val mediaType = directoryModel.mediaType
        return if (mediaType == Constant.IMAGE) getAllImages(directoryModel.path)
        else getAllVideo(directoryModel.path)
    }

    private fun getAllImages(path: String): List<FileModel> {
        val result = arrayListOf<FileModel>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Images.Media.DATE_MODIFIED
        )
        val orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        val imageCursor = context.contentResolver.query(uri, projection, null, null, orderBy)
        imageCursor?.let { cursor ->
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val dateModified = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                if (path == absPath) {
                    val time = cursor.getString(dateModified)
                    Log.e("Time", "Time -> $time")
                    val model = FileModel(
                        path = data
                    )
                    result.add(model)
                }
            }
        }
        return result
    }

    private fun getAllVideo(path: String): List<FileModel> {
        val result = arrayListOf<FileModel>()
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_MODIFIED
        )
        val orderBy = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        val videoCursor =
            context.contentResolver.query(videoUri, videoProjection, null, null, orderBy)
        videoCursor?.let { cursor ->
            val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val dateTaken = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val data = cursor.getString(columnData)
            val absPath = data.dropLastWhile { it != '/' }
            if (path == absPath) {
                val time = cursor.getString(dateTaken)
                Log.e("Time", "Time -> $time")
                val model = FileModel(
                    path = data
                )
                result.add(model)
            }
        }
        return result
    }
}