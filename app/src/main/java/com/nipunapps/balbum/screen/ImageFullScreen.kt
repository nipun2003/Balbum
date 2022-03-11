package com.nipunapps.balbum.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.nipunapps.balbum.components.DeleteComp
import com.nipunapps.balbum.core.Constant.IMAGE
import com.nipunapps.balbum.core.noRippleClickable
import com.nipunapps.balbum.models.FileModel
import com.nipunapps.balbum.ui.Screen
import com.nipunapps.balbum.ui.theme.*
import com.nipunapps.balbum.viewmodel.ImageFullViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
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
    var rotate by remember {
        mutableStateOf(0f)
    }
    val angle by animateFloatAsState(targetValue = rotate)
    val items = imageFullViewModel.items.value
    var editEnable by remember {
        mutableStateOf(false)
    }
    BackHandler(enabled = editEnable) {
        editEnable = false
    }
    LaunchedEffect(
        key1 = pagerState,
    ) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            imageFullViewModel.changeIndex(page)
        }
    }
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
        if (items.isEmpty()) {
            navController.popBackStack()
        } else {
            if (deleteDialogue) {
                DeleteComp(file = imageFullViewModel.deleteItems(),
                    onDeleted = {
                        deleteDialogue = false
                        if (items.size == 1) {
                            navController.navigate(
                                Screen.HomeScreen.route
                            ) {
                                popUpTo(Screen.HomeScreen.route) {
                                    inclusive = true
                                }
                            }
                        } else {
                            imageFullViewModel.reloadData()
                        }
                    },
                    onDeniedOrFailed = {
                        deleteDialogue = false
                    })
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                HorizontalPager(
                    count = items.size,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize(),
                    userScrollEnabled = !editEnable,
                ) { index ->
                    val item = items[index]
                    SinglePager(
                        fileModel = item,
                        modifier = Modifier
                            .fillMaxSize(),
                        angle = angle,
                        mediaType = imageFullViewModel.directoryModel.value.mediaType,
                        onPlayClick = {
                            imageFullViewModel.playVideo()
                        },
                        onDrag = { isNext ->
                            coroutineScope.launch {
                                when {
                                    isNext < 10f && index != items.size - 1 -> {
                                        pagerState.animateScrollToPage(
                                            page = index + 1,
                                        )
                                    }
                                    isNext > -10f && index != 0 -> {
                                        pagerState.animateScrollToPage(
                                            page = index - 1,
                                        )
                                    }
                                }
                            }
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
                            .background(color = MaterialTheme.colors.background)
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
                            .background(color = MaterialTheme.colors.background)
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = BigSpacing),
                        showBack = selectedIndex != 0,
                        showNext = selectedIndex != items.size - 1,
                        onDeleteClick = {
                            deleteDialogue = true
                        },
                        onShareClick = {
                            imageFullViewModel.sendFile()
                        },
                        onPrevClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = pagerState.currentPage - 1
                                )
                            }
                        },
                        onNextClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = pagerState.currentPage + 1
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomEditor(
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showNext: Boolean = false,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_next),
                contentDescription = "Share",
                modifier = Modifier
                    .size(IconSize)
                    .rotate(180f)
                    .padding(SmallSpacing)
                    .noRippleClickable {
                        if (showBack) {
                            onPrevClick()
                        }
                    },
                tint = if(showBack) MaterialTheme.colors.onBackground else Color.Gray.copy(alpha = 0.67f)
            )
            Spacer(modifier = Modifier.size(MediumSpacing))

            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = "Share",
                modifier = Modifier
                    .size(IconSize)
                    .padding(SmallSpacing)
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
                    .padding(SmallSpacing)
                    .noRippleClickable {
                        onDeleteClick()
                    }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_next),
                contentDescription = "Share",
                modifier = Modifier
                    .size(IconSize)
                    .padding(SmallSpacing)
                    .noRippleClickable {
                        if (showNext)
                            onNextClick()
                    },
                tint = if(showNext) MaterialTheme.colors.onBackground else Color.Gray.copy(alpha = 0.67f)
            )
            Spacer(modifier = Modifier.size(MediumSpacing))

        }
        Spacer(modifier = Modifier.size(SmallSpacing))
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
                        .padding(SmallSpacing)
                        .noRippleClickable {
                            onBackPress()
                        },
                )
                Text(
                    text = "${selectedIndex + 1}/$totalItem",
                    style = MaterialTheme.typography.body1
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    modifier = Modifier
                        .size(IconSize)
                        .padding(SmallSpacing)
                        .noRippleClickable {
                            onInfoClick()
                        }
                )
            }
        }
        Spacer(modifier = Modifier.size(PaddingStatusBar))
    }
}

@Composable
fun SinglePager(
    modifier: Modifier = Modifier,
    fileModel: FileModel,
    mediaType: String,
    angle: Float,
    onDrag: (Float) -> Unit,
    onPlayClick: () -> Unit,
    onBoxClick: () -> Unit
) {
    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(angle) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val animateScale = animateFloatAsState(targetValue = scale.value)

    var zoom by remember {
        mutableStateOf(2)
    }
    Box(
        modifier = modifier
            .noRippleClickable {
                onBoxClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (zoom == 0) {
                        scale.value = 1f
                        rotationState.value = 0f
                        offsetX = 0f
                        offsetY = 0f
                        zoom = 2
                    } else {
                        scale.value *= 2f
                        zoom--
                    }
                })
            }
    ) {
        if (mediaType == IMAGE) {
            Image(
                painter = rememberGlidePainter(
                    fileModel.path
                ),
                contentDescription = fileModel.name,
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(rotationState.value)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .scale(
                        scaleX = maxOf(.5f, minOf(100f, animateScale.value)),
                        scaleY = maxOf(.5f, minOf(100f, animateScale.value)),
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            scale.value *= zoom
                            rotationState.value += rotation
                            val x = pan.x * zoom
                            val y = pan.y * zoom
                            val angleRad = angle * PI / 180.0
                            if (scale.value != 1.0f) {
                                offsetX += (x * cos(angleRad) - y * sin(angleRad)).toFloat()
                                offsetY += (x * sin(angleRad) + y * cos(angleRad)).toFloat()
                            } else {
                                if (zoom in 0.97..1.0) {
                                    onDrag(
                                        pan.x
                                    )
                                }
                            }
                        }
                    },
            )
        } else {
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
                contentDescription = "Play",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
                    .padding(SmallSpacing)
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
