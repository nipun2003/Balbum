package com.nipunapps.balbum.core

import android.content.Context
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel

class FileHandler(
    context: Context
) {
    private val contentResolver = context.contentResolver
    fun getAllDirectory(mediaType: String): List<DirectoryModel> {
        val isImage = mediaType == Constant.IMAGE
        val uri = if (isImage) MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            if (isImage) MediaStore.MediaColumns.BUCKET_DISPLAY_NAME else MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.VOLUME_NAME
        )
        val orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        val imageCursor = contentResolver.query(uri, projection, null, null, orderBy)
        imageCursor?.let { cursor ->
            val temp = arrayListOf<DirectoryModel>()
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val buckedIdIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
                val bucketName = cursor.getColumnIndexOrThrow(
                    if (isImage) MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                    else MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                )
                val volumeName = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.VOLUME_NAME)
                val data = cursor.getString(columnData)
                val directoryModel = DirectoryModel(
                    name = cursor.getStringOrNull(bucketName)?:cursor.getStringOrNull(volumeName)?:"",
                    bucketId = cursor.getString(buckedIdIndex),
                    mediaType = mediaType,
                )
                val prevIndex = temp.indexOf(directoryModel)
                if (prevIndex > -1) temp[prevIndex].count++
                else temp.add(directoryModel.apply {
                    firstFilePath = data
                })
                val allPhotoIndex = temp.indexOf(DirectoryModel(
                    name = if(isImage) Constant.ALL_IMAGES else Constant.ALL_VIDEOS,
                    mediaType = mediaType,
                    bucketId = ""
                ))
                if (allPhotoIndex != -1) {
                    temp[allPhotoIndex] = temp[allPhotoIndex].apply {
                        count += 1
                    }
                } else temp.add(DirectoryModel(
                    name = if(isImage) Constant.ALL_IMAGES else Constant.ALL_VIDEOS,
                    mediaType = mediaType,
                    bucketId = ""
                ).apply {
                    firstFilePath = data
                })
            }
            return temp.sortedBy { it.getFilterName() }
        }
        return emptyList()
    }

    fun getAllFiles(mediaType: String): List<FileModel> {
        val isImage = mediaType == Constant.IMAGE
        val result = arrayListOf<FileModel>()
        val uri =
            if (isImage) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DURATION,
        )
        val orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        val fileCursor = contentResolver.query(uri, projection, null, null, orderBy)
        fileCursor?.let { cursor ->
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val dateModified =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val data = cursor.getString(columnData)
                val time = cursor.getLong(dateModified)
                val size: Double = cursor.getLong(sizeIndex) / 1024.0
                val model = FileModel(
                    path = data,
                    lastModified = time,
                    size = size,
                    id = cursor.getString(idIndex).toLong(),
                    duration = if (!isImage) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)) / 1000L
                    else null
                )
                result.add(model)
            }
        }
        return result
    }

    fun getAllFilesInsideDirectory(path: String, mediaType: String): List<FileModel> {
        val isImage = mediaType == Constant.IMAGE
        val result = arrayListOf<FileModel>()
        val selection = MediaStore.MediaColumns.BUCKET_ID
        val selectionArg = arrayOf(path)
        val orderBy = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
        val uri =
            if (isImage) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DURATION
        )
        val imageCursor =
            contentResolver.query(uri, projection, "$selection=?", selectionArg, orderBy)
        imageCursor?.let { cursor ->
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val dateModified =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val data = cursor.getString(columnData)
                val time = cursor.getLong(dateModified)
                val size: Double = cursor.getLong(sizeIndex) / 1024.0
                val model = FileModel(
                    path = data,
                    lastModified = time,
                    size = size,
                    id = cursor.getString(idIndex).toLong(),
                    duration = if (!isImage) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)) / 1000L
                    else null
                )
                result.add(model)
            }
        }
        return result
    }
}