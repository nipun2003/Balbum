package com.nipunapps.balbum.components

import android.app.Activity.RESULT_OK
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.nipunapps.balbum.core.Constant

@Composable
fun DeleteComp(
    file: List<Uri>,
    onDeleted: () -> Unit,
    onDeniedOrFailed: () -> Unit = {}
) {
    val context = LocalContext.current
    val intentSenderLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.e("Nipun", "Photo deleted successfully")
                onDeleted()
            } else {
                onDeniedOrFailed()
            }
        }
    LaunchedEffect(key1 = file) {
        val intentSender =
            MediaStore.createDeleteRequest(context.contentResolver, file).intentSender
        intentSenderLauncher.launch(
            IntentSenderRequest.Builder(intentSender).build()
        )
    }
}