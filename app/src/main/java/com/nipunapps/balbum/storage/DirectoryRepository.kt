package com.nipunapps.balbum.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.Resource
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.models.FileModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DirectoryRepository(
    private val context: Context
) {

    fun getData(directoryModel: DirectoryModel): List<FileModel> {
        val mediaType = directoryModel.mediaType
        val isFilter = directoryModel.path != "/All Photos/"
        val isVideoFilter = directoryModel.path != Constant.ALL_VIDEOS
        return if (mediaType == Constant.IMAGE) getAllImages(
            directoryModel.path,
            isFilter = isFilter
        )
        else getAllVideo(directoryModel.path, isFilter = isVideoFilter)
    }

    fun deleteFiles(list: List<String>,directoryModel: DirectoryModel) : Flow<Resource<List<FileModel>>> = flow {
        emit(Resource.Loading<List<FileModel>>())
        try {
            list.forEach { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            emit(Resource.Success<List<FileModel>>(
                data = getData(directoryModel)
            ))
        }catch (e : Exception){
            emit(Resource.Error<List<FileModel>>(
                data = getData(directoryModel),
                message = "Error deleting file"
            ))
        }
    }

    fun sendMultipleFiles(list: List<String>,mediaType : String){
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

    private fun getAllImages(path: String, isFilter: Boolean = true): List<FileModel> {
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
                val dateModified =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                if (!isFilter) {
                    val time = cursor.getString(dateModified)
                    val model = FileModel(
                        path = data
                    )
                    result.add(model)
                } else {
                    if (path == absPath) {
                        val time = cursor.getString(dateModified)
                        val model = FileModel(
                            path = data
                        )
                        result.add(model)
                    }
                }
            }
        }
        return result
    }

    private fun getAllVideo(path: String, isFilter: Boolean = true): List<FileModel> {
        val result = arrayListOf<FileModel>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Video.Media.DURATION
        )
        val orderBy = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        val videoCursor = context.contentResolver.query(uri, projection, null, null, orderBy)
        videoCursor?.let { cursor ->
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val duration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                if (!isFilter) {
                    val time = cursor.getLong(duration)
                    val model = FileModel(
                        path = data
                    )
                    result.add(model)
                } else {
                    if (path == absPath) {
                        val time = cursor.getLong(duration) / 1000L
                        Log.e("Duration", "Duration -> $time")
                        val model = FileModel(
                            path = data,
                            duration = time
                        )
                        result.add(model)
                    }
                }
            }
        }
        return result
    }
}