package com.nipunapps.balbum.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.FileHandler
import com.nipunapps.balbum.models.DirectoryModel

class StorageRepository(private val context: Context) {

    private val fileHandler = FileHandler(context = context)
    fun getAllDirectory(): List<DirectoryModel> {
        return fileHandler.getAllDirectory(Constant.IMAGE) + fileHandler.getAllDirectory(Constant.VIDEO)
    }

    fun sendMultipleFiles(list: List<DirectoryModel>) {
        list.forEach { directoryModel ->
            val mediaType = directoryModel.mediaType
            val datas = getImageVideoInSideDirectory(
                directoryModel.name,
                mediaType
            ).map { Uri.parse(it) } as ArrayList<Uri>
            val mimeType = if (mediaType == Constant.IMAGE) "image/*" else "video/*"
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                putExtra(Intent.EXTRA_SUBJECT, "here are some files")
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, datas)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private fun getImageVideoInSideDirectory(path: String, type: String): ArrayList<String> {
        val result = arrayListOf<String>()
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageProjection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val videoProjection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val imageCursor = context.contentResolver.query(imageUri, imageProjection, null, null)
        val videoCursor = context.contentResolver.query(videoUri, videoProjection, null, null)
        if (type == Constant.IMAGE) {
            imageCursor?.let { cursor ->
                while (cursor.moveToNext()) {
                    val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val data = cursor.getString(columnData)
                    val absPath = data.dropLastWhile { it != '/' }
                    if (absPath == path) {
                        result.add(data)
                    }
                }
            }
        } else {
            videoCursor?.let { cursor ->
                while (cursor.moveToNext()) {
                    val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val data = cursor.getString(columnData)
                    val absPath = data.dropLastWhile { it != '/' }
                    if (absPath == path) {
                        result.add(data)
                    }
                }
            }
        }
        return result
    }

    fun deleteDirectory(directories: List<DirectoryModel>): List<Uri> {
        directories.forEach { directoryModel ->
            return fileHandler.getAllFilesInsideDirectory(
                directoryModel.bucketId,
                mediaType = directoryModel.mediaType
            )
                .map {
                    it.getMediaUri(directoryModel.mediaType)
                }
        }
        return emptyList()
    }

}