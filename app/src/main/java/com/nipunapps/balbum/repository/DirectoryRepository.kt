package com.nipunapps.balbum.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.nipunapps.balbum.BuildConfig
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.FileHandler
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel
import java.io.File

class DirectoryRepository(
    private val context: Context
) {
    private val fileHandler = FileHandler(context)
    fun getData(directoryModel: DirectoryModel): List<FileModel> {
        val mediaType = directoryModel.mediaType
        return if (directoryModel.bucketId.isEmpty()) fileHandler.getAllFiles(mediaType)
        else fileHandler.getAllFilesInsideDirectory(path = directoryModel.bucketId, mediaType = mediaType)
    }

    fun playVideo(path: String) {
        val uri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            File(path)
        )
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(uri, "video/*")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("Nipun", e.message.toString())
        }
    }

    fun sendFile(path: String, mediaType: String) {
        val datas = Uri.parse(path)
        val mimeType = if (mediaType == Constant.IMAGE) "image/*" else "video/*"
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putExtra(Intent.EXTRA_SUBJECT, "here are some files")
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, datas)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun sendMultipleFiles(list: List<String>, mediaType: String) {
        val datas = list.map { Uri.parse(it) } as ArrayList<Uri>
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