package com.nipunapps.balbum.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.glide.rememberGlidePainter
import com.nipunapps.balbum.R
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.ui.theme.*
import com.nipunapps.balbum.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val directories = homeViewModel.directoryState.value
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            HomeTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(
                        maxOf(ToolbarMinSize, ToolbarMaxSize - scrollState.value / 1200f * 0.6f)
                    )
                    .padding(horizontal = BigSpacing)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState, enabled = true)
                .fillMaxWidth()
                .padding(MediumSpacing)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                crossAxisAlignment = FlowCrossAxisAlignment.Start,
                mainAxisAlignment = MainAxisAlignment.SpaceAround,
                crossAxisSpacing = SmallSpacing,
                mainAxisSpacing = MediumSpacing
            ) {
                directories.forEach {
                    SingleDirectory(
                        directoryModel = it,
                        modifier = Modifier
                            .fillMaxWidth(0.28f)
                    )
                }
            }

        }
    }
}

@Composable
fun SingleDirectory(
    modifier: Modifier = Modifier,
    directoryModel: DirectoryModel
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ExtraSmallSpacing)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Image(
            painter = rememberGlidePainter(
                directoryModel.firstFilePath
            ),
            contentDescription = directoryModel.getName(),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(ExtraSmallSpacing)),
            contentScale = ContentScale.FillBounds,
            alignment = Alignment.Center,
        )
        Spacer(modifier = Modifier.size(BigStroke))
        Text(
            text = directoryModel.getName(),
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${directoryModel.count} items",
            style = MaterialTheme.typography.overline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.size(PaddingStatusBar))
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = "Albums",
                style = MaterialTheme.typography.h1,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopStart)
            )

            Row(
                modifier = Modifier
                    .padding(
                        bottom = ExtraSmallSpacing,
                        end = ExtraSmallSpacing
                    )
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "New Folder",
                    modifier = Modifier
                        .size(IconSize)
                        .padding(ExtraSmallSpacing),
                    tint = MaterialTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.size(SmallSpacing))
                Icon(
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = "Setting",
                    modifier = Modifier
                        .size(IconSize)
                        .padding(ExtraSmallSpacing),
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
    }

}