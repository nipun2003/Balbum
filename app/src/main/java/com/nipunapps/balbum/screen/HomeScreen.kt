package com.nipunapps.balbum.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.glide.rememberGlidePainter
import com.nipunapps.balbum.R
import com.nipunapps.balbum.components.DeleteDialogue
import com.nipunapps.balbum.core.UIEvent
import com.nipunapps.balbum.core.noRippleClickable
import com.nipunapps.balbum.models.DirectoryModel
import com.nipunapps.balbum.ui.Screen
import com.nipunapps.balbum.ui.theme.*
import com.nipunapps.balbum.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val directories = homeViewModel.directoryState.value
    val scrollState = rememberScrollState()
    val scaffoldState = rememberScaffoldState()
    var selectionMode by remember {
        mutableStateOf(false)
    }
    var deleteDialogue by remember {
        mutableStateOf(false)
    }
    BackHandler(enabled = selectionMode) {
        homeViewModel.toggleAllSelect(false)
        selectionMode = false
    }
    LaunchedEffect(
        key1 = true,
        block = {
            homeViewModel.eventFlow.collectLatest { event->
                when(event) {
                    is UIEvent.ShowSnackbar -> {
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = event.message
                        )
                    }
                }
            }
        }
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if(event == Lifecycle.Event.ON_RESUME) {
                    homeViewModel.updateItems()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            HomeTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(
                        maxOf(ToolbarMinSize, ToolbarMaxSize - scrollState.value / 1200f * 0.6f)
                    )
                    .padding(horizontal = BigSpacing),
                selectionModel = selectionMode,
                selectedItem = directories.count { it.isSelected },
                totalItem = directories.size,
                onSelectionModelClick = { selectionMode = true },
                onCrossClick = {
                    selectionMode = false
                    homeViewModel.toggleAllSelect(false)
                },
                onSelectAllClick = { value ->
                    homeViewModel.toggleAllSelect(value)
                },
                onDeleteClick = {
                    deleteDialogue = true
                },
                onShareClick = {
                    homeViewModel.shareFiles()
                }
            )
        }
    ) {
        if (deleteDialogue) {
            DeleteDialogue(
                expand = deleteDialogue,
                onYesClick = {
                    homeViewModel.deleteItems()
                    deleteDialogue = false
                    selectionMode = false
                },
                onDismissClick = {
                    deleteDialogue = false
                },
                onDismissRequest = {
                    deleteDialogue = false
                }
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState, enabled = true)
                .fillMaxWidth()
                .padding(MediumSpacing)
        ) {
            Text(
                text = "Images",
                style = MaterialTheme.typography.h3
            )
            Spacer(modifier = Modifier.size(MediumSpacing))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                crossAxisAlignment = FlowCrossAxisAlignment.Start,
                mainAxisAlignment = MainAxisAlignment.SpaceAround,
                crossAxisSpacing = SmallSpacing,
                mainAxisSpacing = MediumSpacing
            ) {
                for (index in 0 until homeViewModel.getLastImageIndex()) {
                    val item = directories[index]
                    SingleDirectory(
                        directoryModel = item,
                        modifier = Modifier
                            .fillMaxWidth(0.28f),
                        selectionMode = selectionMode,
                        onItemClick = {
                            if (selectionMode) {
                                homeViewModel.toggleSelection(index)
                            } else {
                                val arg = URLEncoder.encode(
                                    Json.encodeToString(item),
                                    "utf-8"
                                )
                                navController.navigate(Screen.DetailScreen.route + "/$arg")
                            }
                        },
                        onItemLongClick = {
                            homeViewModel.toggleSelection(index = index)
                            selectionMode = true
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.size(BigSpacing))
            Text(
                text = "Videos",
                style = MaterialTheme.typography.h3
            )
            Spacer(modifier = Modifier.size(MediumSpacing))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                crossAxisAlignment = FlowCrossAxisAlignment.Start,
                mainAxisAlignment = MainAxisAlignment.SpaceAround,
                crossAxisSpacing = SmallSpacing,
                mainAxisSpacing = MediumSpacing
            ) {
                for (index in homeViewModel.getLastImageIndex() until directories.size) {
                    val item = directories[index]
                    SingleDirectory(
                        directoryModel = item,
                        modifier = Modifier
                            .fillMaxWidth(0.28f),
                        selectionMode = selectionMode,
                        onItemClick = {
                            if (selectionMode) {
                                homeViewModel.toggleSelection(index)
                            } else {
                                val arg = URLEncoder.encode(
                                    Json.encodeToString(item),
                                    "utf-8"
                                )
                                navController.navigate(Screen.DetailScreen.route + "/$arg")
                            }
                        },
                        onItemLongClick = {
                            homeViewModel.toggleSelection(index = index)
                            selectionMode = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SingleDirectory(
    modifier: Modifier = Modifier,
    directoryModel: DirectoryModel,
    selectionMode: Boolean = false,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ExtraSmallSpacing))
            .clickable {
                onItemClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onItemClick()
                    },
                    onLongPress = {
                        onItemLongClick()
                    }
                )
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(ExtraSmallSpacing))
        ) {
            Image(
                painter = rememberGlidePainter(
                    directoryModel.firstFilePath
                ),
                contentDescription = directoryModel.getName(),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(ExtraSmallSpacing)),
                contentScale = ContentScale.FillBounds,
                alignment = Alignment.Center,
                alpha = if (selectionMode) 0.55f else 1.0f
            )
            if (selectionMode) {
                Icon(
                    painter = painterResource(
                        id = if (directoryModel.isSelected) R.drawable.ic_check_box
                        else R.drawable.ic_check_box_outline
                    ),
                    contentDescription = "check box",
                    modifier = Modifier
                        .size(IconSize)
                        .padding(ExtraSmallSpacing)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(ExtraSmallSpacing)),
                    tint = if (directoryModel.isSelected) Color.Blue else MaterialTheme.colors.onBackground
                )
            }
        }
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
    modifier: Modifier = Modifier,
    selectionModel: Boolean = false,
    selectedItem: Int = 0,
    totalItem: Int = 0,
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSelectAllClick: (Boolean) -> Unit = {},
    onCrossClick: () -> Unit = {},
    onSelectionModelClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},

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
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .align(Alignment.TopStart)
            )
            if (!selectionModel) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4.2f)
                        .padding(
                            bottom = ExtraSmallSpacing,
                            end = ExtraSmallSpacing
                        )
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_select_mode),
                        contentDescription = "Select Mode",
                        modifier = Modifier
                            .size(IconSize)
                            .padding(ExtraSmallSpacing)
                            .clickable {
                                onSelectionModelClick()
                            },
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
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4.2f)
                        .background(color = MaterialTheme.colors.surface)
                        .zIndex(100f)
                        .padding(
                            top = BigSpacing,
                            bottom = ExtraSmallSpacing,
                            end = ExtraSmallSpacing,
                            start = ExtraSmallSpacing
                        )
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Close Selection",
                            modifier = Modifier
                                .size(IconSize)
                                .rotate(45f)
                                .noRippleClickable {
                                    onCrossClick()
                                },
                            tint = MaterialTheme.colors.onBackground
                        )
                        Text(
                            text = if (selectedItem == 0) "Select Items" else "$selectedItem/$totalItem",
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Share",
                            modifier = Modifier
                                .size(IconSize)
                                .padding(ExtraSmallSpacing)
                                .noRippleClickable {
                                    if (selectedItem == 1) {
                                        onShareClick()
                                    }
                                },
                            tint = if (selectedItem == 1) MaterialTheme.colors.onBackground else Color.Gray

                        )
                        Spacer(modifier = Modifier.size(SmallSpacing))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            modifier = Modifier
                                .size(IconSize)
                                .padding(ExtraSmallSpacing)
                                .noRippleClickable {
                                    if (selectedItem > 0) {
                                        onDeleteClick()
                                    }
                                },
                            tint = if (selectedItem > 0) MaterialTheme.colors.onBackground else Color.Gray
                        )
                        Spacer(modifier = Modifier.size(SmallSpacing))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_select_all),
                            contentDescription = "Select All",
                            modifier = Modifier
                                .size(IconSize)
                                .padding(ExtraSmallSpacing)
                                .noRippleClickable {
                                    onSelectAllClick(selectedItem != totalItem)
                                },
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                }
            }
        }
    }

}