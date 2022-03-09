package com.nipunapps.balbum.ui

sealed class Screen(val route: String) {
    object HomeScreen: Screen("home")
    object DetailScreen : Screen("details")
    object ImageFullScreen : Screen("image")
}
