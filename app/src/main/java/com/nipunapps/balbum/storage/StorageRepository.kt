package com.nipunapps.balbum.storage

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.nipunapps.balbum.core.Constant
import com.nipunapps.balbum.core.Resource
import com.nipunapps.balbum.models.DirectoryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class StorageRepository(private val context: Context) {

    fun getAllDirectory() : List<DirectoryModel>{
        return getAllImageDirectory()+getAllVideoDirectory()
    }

    fun deleteDirectory(directories : List<DirectoryModel>) : Flow<Resource<List<DirectoryModel>>> = flow {
        emit(Resource.Loading<List<DirectoryModel>>())
        try {
            directories.forEach { directoryModel ->
                if (directoryModel.mediaType == Constant.IMAGE) {
                    deleteAllImagesFromDirectory(directoryModel.path)
                } else deleteAllVideoFromDirectory(directoryModel.path)
                emit(
                    Resource.Success<List<DirectoryModel>>(
                        data = getAllImageDirectory() + getAllVideoDirectory()
                    )
                )
            }
        }catch (e : Exception){
            Log.e("Nipun",e.message.toString())
            emit(Resource.Error<List<DirectoryModel>>(
                message = "Something Went wrong on deleting items"
            ))
        }
    }

    @SuppressLint("Recycle")
    private fun getAllImageDirectory(): List<DirectoryModel> {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        val imageCursor = context.contentResolver.query(uri, projection, null, null, orderBy)
        imageCursor?.let { cursor ->
            val temp = arrayListOf<DirectoryModel>()
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                val directoryModel = DirectoryModel(absPath)
                val prevIndex = temp.indexOf(directoryModel)
                if (prevIndex > -1) temp[prevIndex].count++
                else temp.add(directoryModel.apply {
                    firstFilePath = data
                })
                val allPhotoIndex = temp.indexOf(DirectoryModel())
                if(allPhotoIndex != -1){
                    temp[allPhotoIndex] = temp[allPhotoIndex].apply {
                        count += 1
                    }
                }else temp.add(DirectoryModel().apply {
                    firstFilePath = data
                })
            }
            return temp.sortedBy { it.getName() }
        }
        return emptyList()
    }

    private suspend fun deleteAllImagesFromDirectory(path : String){
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val imageCursor = context.contentResolver.query(uri, projection, null, null)
        imageCursor?.let { cursor ->
            while (cursor.moveToNext()){
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                if(absPath == path){
                    val file = File(data)
                    if(file.exists()){
                        file.delete()
                    }
                }
            }
        }
    }

    private suspend fun deleteAllVideoFromDirectory(path : String){
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val videoCursor = context.contentResolver.query(uri, projection, null, null)
        videoCursor?.let { cursor ->
            while (cursor.moveToNext()){
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                if(absPath == path){
                    val file = File(data)
                    if(file.exists()){
                        file.delete()
                    }
                }
            }
        }
    }


    @SuppressLint("Recycle")
    private fun getAllVideoDirectory(): List<DirectoryModel> {
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val orderBy = MediaStore.Video.Media.DATE_MODIFIED+ " DESC";
        val videoCursor = context.contentResolver.query(uri, projection, null, null, orderBy)
        videoCursor?.let { cursor ->
            val temp = arrayListOf<DirectoryModel>()
            while (cursor.moveToNext()) {
                val columnData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val data = cursor.getString(columnData)
                val absPath = data.dropLastWhile { it != '/' }
                val directoryModel = DirectoryModel(absPath,Constant.VIDEO)
                val prevIndex = temp.indexOf(directoryModel)
                if (prevIndex > -1) temp[prevIndex].count++
                else temp.add(directoryModel.apply {
                    firstFilePath = data
                })
            }
            return temp.sortedBy { it.getName() }
        }
        return emptyList()
    }
}