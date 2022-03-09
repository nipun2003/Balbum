package com.nipunapps.balbum.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.nipunapps.balbum.R
import com.nipunapps.balbum.core.showToast
import com.nipunapps.balbum.screen.HomeScreen
import com.nipunapps.balbum.ui.theme.BAlbumTheme
import com.nipunapps.balbum.ui.theme.enterAnimation
import com.nipunapps.balbum.ui.theme.exitAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = resources.getColor(R.color.transparent)
        }
        setContent {
            BAlbumTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberAnimatedNavController()
                    AnimatedNavHost(
                        navController =navController,
                        startDestination = Screen.HomeScreen.route
                    ){
                        composable(
                            route = Screen.HomeScreen.route,
                            exitTransition = { exitAnimation(-300) },
                            popEnterTransition = { enterAnimation(-300) }
                        ){
                            HomeScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            showToast(this, getString(R.string.allow_us))
            startActivity(intent)
        }
    }
}