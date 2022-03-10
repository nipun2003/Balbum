package com.nipunapps.balbum.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.nipunapps.balbum.R
import com.nipunapps.balbum.components.DeleteDialogue
import com.nipunapps.balbum.core.Constant.IMAGE
import com.nipunapps.balbum.core.noRippleClickable
import com.nipunapps.balbum.models.FileModel
import com.nipunapps.balbum.ui.theme.*
import com.nipunapps.balbum.viewmodel.ImageFullViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun ImageFullScreen(
    navController: NavController,
    imageFullViewModel: ImageFullViewModel = hiltViewModel()
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    val pagerState = rememberPagerState(
        initialPage = imageFullViewModel.index.value
    )
    var deleteDialogue by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = imageFullViewModel.index.value
    var showEditors by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(
        key1 = pagerState,
    ) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            imageFullViewModel.changeIndex(page)
        }
    }
    val items = imageFullViewModel.items.value
    BottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetContent = {
            Info(
                fileModel = items[selectedIndex],
                modifier = Modifier
                    .fillMaxWidth()
            )

        },
        sheetPeekHeight = 0.dp
    ) {
        if (deleteDialogue) {
            DeleteDialogue(
                expand = deleteDialogue,
                onYesClick = {
                    imageFullViewModel.deleteItems()
                    deleteDialogue = false
                },
                onDismissClick = {
                    deleteDialogue = false
                },
                onDismissRequest = {
                    deleteDialogue = false
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HorizontalPager(
                count = items.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { index ->
                val item = items[index]
                SinglePager(
                    fileModel = item,
                    modifier = Modifier
                        .fillMaxSize(),
                    mediaType = imageFullViewModel.directoryModel.value.mediaType,
                    onPlayClick = {
                        imageFullViewModel.playVideo()
                    }
                ) {
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.collapse()
                        showEditors = !showEditors
                    }
                }
            }
            if (showEditors) {
                TopEditor(
                    selectedIndex = selectedIndex,
                    totalItem = items.size,
                    modifier = Modifier
                        .zIndex(10f)
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = BigSpacing),
                    onBackPress = {
                        navController.popBackStack()
                    }
                ) {
                    coroutineScope.launch {
                        if (scaffoldState.bottomSheetState.isCollapsed) {
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            scaffoldState.bottomSheetState.collapse()
                        }
                    }
                }

                BottomEditor(
                    modifier = Modifier
                        .zIndex(10f)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = BigSpacing),
                    onDeleteClick = {
                        deleteDialogue = true
                    },
                    onShareClick = {
                        imageFullViewModel.sendFile()
                    }
                )
            }
        }
    }
}

@Composable
fun BottomEditor(
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.size(PaddingStatusBar))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = "Share",
                modifier = Modifier
                    .size(IconSize)
                    .padding(ExtraSmallSpacing)
                    .noRippleClickable {
                        onShareClick()
                    }
            )
            Spacer(modifier = Modifier.size(MediumSpacing))
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                modifier = Modifier
                    .size(IconSize)
                    .padding(ExtraSmallSpacing)
                    .noRippleClickable {
                        onDeleteClick()
                    }
            )
        }
        Spacer(modifier = Modifier.size(PaddingStatusBar))
    }
}

@Composable
fun TopEditor(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    totalItem: Int,
    onBackPress: () -> Unit,
    onInfoClick: () -> Unit
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.size(PaddingStatusBar))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(IconSize)
                        .padding(ExtraSmallSpacing)
                        .noRippleClickable {
                            onBackPress()
                        },
                )
                Text(
                    text = "${selectedIndex + 1}/$totalItem",
                    style = MaterialTheme.typography.body1
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                modifier = Modifier
                    .size(IconSize)
                    .padding(ExtraSmallSpacing)
                    .noRippleClickable {
                        onInfoClick()
                    }
            )
        }
        Spacer(modifier = Modifier.size(PaddingStatusBar))
    }
}

@Composable
fun SinglePager(
    modifier: Modifier = Modifier,
    fileModel: FileModel,
    mediaType: String,
    onPlayClick : () -> Unit,
    onBoxClick: () -> Unit
) {
    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val animateScale = animateFloatAsState(targetValue = scale.value)

    var zoom by remember {
        mutableStateOf(2)
    }
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                if (mediaType == IMAGE) {
                    detectTapGestures(
                        onTap = {
                            onBoxClick()
                        },
                        onDoubleTap = {
                            if (zoom == 0) {
                                offsetX = 0f
                                offsetY = 0f
                                scale.value = 1f
                                rotationState.value = 0f
                                zoom = 2
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                                scale.value *= 2
                                rotationState.value = 0f
                                zoom -= 1
                            }
                        }
                    )
                }
            }
    ) {
        if(mediaType == IMAGE) {
            Image(
                painter = rememberGlidePainter(
                    fileModel.path
                ),
                contentDescription = fileModel.name,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .graphicsLayer(
                        scaleX = maxOf(.5f, minOf(100f, animateScale.value)),
                        scaleY = maxOf(.5f, minOf(100f, animateScale.value)),
                        rotationZ = rotationState.value
                    ),
            )
        }else {
            Image(
                painter = rememberGlidePainter(
                    fileModel.path
                ),
                contentDescription = fileModel.name,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .aspectRatio(1.3f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                alpha = 0.67f
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription ="Play",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
                    .padding(ExtraSmallSpacing)
                    .clickable {
                        onPlayClick()
                    }
            )
        }
    }
}


@Composable
fun Info(
    modifier: Modifier = Modifier,
    fileModel: FileModel,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(
            topStart = SmallSpacing, topEnd = SmallSpacing
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ExtraBigSpacing),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "File Name",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = fileModel.name,
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.size(MediumSpacing))
            Text(
                text = "Size",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "${fileModel.size} kb",
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.size(MediumSpacing))
            Text(
                text = "Path : ${fileModel.path}",
                style = MaterialTheme.typography.overline
            )
        }
    }
}