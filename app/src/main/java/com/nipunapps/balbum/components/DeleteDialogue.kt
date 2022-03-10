package com.nipunapps.balbum.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nipunapps.balbum.R
import com.nipunapps.balbum.ui.theme.MediumSpacing

@Composable
fun DeleteDialogue(
    expand: Boolean = false,
    onYesClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    onDismissRequest : () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = {
                           onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = {
                onYesClick()
            }) {
                Text(
                    text = "Yes",
                    style = MaterialTheme.typography.button
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissClick()
            }) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.button
                )
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.delete_warn),
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(MediumSpacing)
            )
        }
    )
}