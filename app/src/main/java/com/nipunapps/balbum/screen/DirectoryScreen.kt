package com.nipunapps.balbum.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nipunapps.balbum.ui.theme.PaddingStatusBar
import com.nipunapps.balbum.ui.theme.SmallSpacing
import com.nipunapps.balbum.viewmodel.DirectoryViewModel

@Composable
fun DirectoryScreen(
    navController: NavController,
    directoryViewModel: DirectoryViewModel = hiltViewModel()
) {
    val items = directoryViewModel.items.value.items
    val scrollState = rememberScrollState()
    Scaffold() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState, enabled = true)
        ) {
            Spacer(modifier = Modifier.size(PaddingStatusBar))
            items.forEachIndexed { index, fileModel ->
                if (index > 0) Spacer(modifier = Modifier.size(SmallSpacing))
                Text(
                    text = fileModel.name,
                    style = MaterialTheme.typography.body1
                )
            }
        }

    }
}