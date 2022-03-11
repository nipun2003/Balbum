package com.nipunapps.balbum.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.accompanist.glide.rememberGlidePainter
import com.nipunapps.balbum.R
import com.nipunapps.balbum.components.DeleteComp
import com.nipunapps.balbum.core.UIEvent
import com.nipunapps.balbum.core.noRippleClickable
import com.nipunapps.balbum.core.toTimeFormat
import com.nipunapps.balbum.models.FileModel
import com.nipunapps.balbum.ui.Screen
import com.nipunapps.balbum.ui.theme.*
import com.nipunapps.balbum.viewmodel.DirectoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectoryScreen(
    navController: NavController,
    directoryViewModel: DirectoryViewModel = hiltViewModel()
) {
    val items = directoryViewModel.items.value
    var deleteDialogue by remember {
        mutableStateOf(false)
    }
    var selectionMode by remember {
        mutableStateOf(false)
    }
    val scaffoldState = rememberScaffoldState()
    BackHandler(enabled = selectionMode) {
        directoryViewModel.toggleAllSelect(false)
        selectionMode = false
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    directoryViewModel.updateItems()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
    LaunchedEffect(
        key1 = true,
        block = {
            directoryViewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is UIEvent.ShowSnackbar -> {
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = event.message
                        )
                    }
                }
            }
        }
    )
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            DetailTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = BigSpacing),
                selectionMode = selectionMode,
                selectedItem = items.count { it.isSelected },
                totalItem = items.size,
                title = directoryViewModel.directoryModel.value.getFilterName(),
                onSelectionModelClick = {
                    selectionMode = true
                },
                onCrossClick = {
                    directoryViewModel.toggleAllSelect(false)
                    selectionMode = false
                },
                onDeleteClick = {
                    deleteDialogue = true
                },
                onSelectAllClick = {
                    directoryViewModel.toggleAllSelect(it)
                },
                onShareClick = {
                    directoryViewModel.sendFiles()
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    ) {
        if (deleteDialogue) {
            DeleteComp(
                file = directoryViewModel.deleteItems(),
                onDeleted = {
                    deleteDialogue = false
                    selectionMode = false
                    if (items.size == 1) {
                        navController.navigate(
                            Screen.HomeScreen.route
                        ) {
                            popUpTo(Screen.HomeScreen.route) {
                                inclusive = true
                            }
                        }
                    } else {
                        directoryViewModel.updateItems()
                    }
                },
                onDeniedOrFailed = {
                    selectionMode = false
                    deleteDialogue = false
                }
            )
        }
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            cells = GridCells.Fixed(4)
        ) {
            items(items.size) { index ->
                val item = items[index]
                SingleFileComp(
                    fileModel = item,
                    selectionMode = selectionMode,
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .aspectRatio(1f),
                    onItemClick = {
                        if (selectionMode) {
                            directoryViewModel.toggleSelection(index = index)
                        } else {
                            val arg = URLEncoder.encode(
                                Json.encodeToString(directoryViewModel.directoryModel.value),
                                "utf-8"
                            )
                            navController.navigate(Screen.ImageFullScreen.route + "/$arg/$index")
                        }
                    },
                    onItemLongClick = {
                        directoryViewModel.toggleSelection(index = index)
                        selectionMode = true
                    }
                )
            }
        }
    }
}

@Composable
fun SingleFileComp(
    modifier: Modifier = Modifier,
    fileModel: FileModel,
    selectionMode: Boolean = false,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        border = BorderStroke(
            width = MediumStroke,
            color = Black
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onItemClick()
                        },
                        onLongPress = {
                            onItemLongClick()
                        }
                    )
                }
        ) {
            Image(
                painter = rememberGlidePainter(
                    fileModel.path
                ),
                contentDescription = fileModel.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(ExtraSmallSpacing)),
                contentScale = ContentScale.FillBounds,
                alignment = Alignment.Center,
                alpha = if (selectionMode) 0.55f else 1.0f
            )
            fileModel.duration?.let { duration ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Black.copy(0.1f),
                                    Black.copy(0.2f),
                                    Black.copy(0.3f),
                                )
                            )
                        )
                        .padding(ExtraSmallSpacing)
                ) {
                    Text(
                        text = duration.toTimeFormat(),
                        style = MaterialTheme.typography.overline,
                    )
                }
            }
            if (selectionMode) {
                Icon(
                    painter = painterResource(
                        id = if (fileModel.isSelected) R.drawable.ic_check_box
                        else R.drawable.ic_check_box_outline
                    ),
                    contentDescription = "check box",
                    modifier = Modifier
                        .size(IconSize)
                        .padding(SmallSpacing)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(ExtraSmallSpacing)),
                    tint = if (fileModel.isSelected) Color.Blue else MaterialTheme.colors.onBackground
                )
            }
        }
    }
}

@Composable
fun DetailTopBar(
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    title: String = "",
    selectedItem: Int = 0,
    totalItem: Int = 0,
    onBackPressed: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSelectAllClick: (Boolean) -> Unit = {},
    onCrossClick: () -> Unit = {},
    onSelectionModelClick: () -> Unit = {},

    ) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.size(PaddingStatusBar))
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!selectionMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(IconSize)
                            .padding(SmallSpacing)
                            .noRippleClickable {
                                onBackPressed()
                            },
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .align(Alignment.CenterEnd)
                        .padding(
                            bottom = ExtraSmallSpacing,
                            end = ExtraSmallSpacing
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_select_mode),
                        contentDescription = "Select Mode",
                        modifier = Modifier
                            .size(IconSize)
                            .padding(SmallSpacing)
                            .clickable {
                                onSelectionModelClick()
                            },
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(100f),
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
                                .padding(SmallSpacing)
                                .noRippleClickable {
                                    if (selectedItem > 0) {
                                        onShareClick()
                                    }
                                },
                            tint = if (selectedItem > 0) MaterialTheme.colors.onBackground else Color.Gray

                        )
                        Spacer(modifier = Modifier.size(SmallSpacing))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            modifier = Modifier
                                .size(IconSize)
                                .padding(SmallSpacing)
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
                                .padding(SmallSpacing)
                                .noRippleClickable {
                                    onSelectAllClick(selectedItem != totalItem)
                                },
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(PaddingStatusBar))
    }

}