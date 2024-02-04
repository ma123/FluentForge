package com.identic.fluentforge.ui.screens.main

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.identic.fluentforge.R
import com.identic.fluentforge.dataReader.remote.utils.NetworkObserver
import com.identic.fluentforge.ui.navigation.NavGraph
import com.identic.fluentforge.ui.navigation.Screens
import com.identic.fluentforge.ui.screens.viewmodels.SettingsViewModel
import com.identic.fluentforge.ui.screens.viewmodels.ThemeMode

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun MainScreen(
    startDestination: String,
    networkStatus: NetworkObserver.Status,
    settingsViewModel: SettingsViewModel,
    startService: () -> Unit
) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = MaterialTheme.colorScheme.background,
        darkIcons = settingsViewModel.getCurrentTheme() == ThemeMode.Light
    )

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController
            )
        }, containerColor = MaterialTheme.colorScheme.background
    ) {
        NavGraph(
            startDestination = startDestination,
            navController = navController,
            networkStatus = networkStatus,
            startService = startService
        )
    }
}

@Composable
fun BottomBar(
    navController: NavHostController
) {
    var activePageIndex by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = activePageIndex != 0) {
        activePageIndex = 0
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val pages = listOf(
            Page(
                icon = ImageVector.vectorResource(R.drawable.broadcast),
                title = R.string.title_speak,
                Screens.SpeakScreen.route
            ),
            Page(
                icon = ImageVector.vectorResource(R.drawable.read),
                title = R.string.title_library,
                Screens.HomeScreen.route
            ),
            Page(
                icon = ImageVector.vectorResource(R.drawable.listen),
                title = R.string.title_radio,
                Screens.RadioScreen.route
            ),
        )

        NavigationBar(
        ) {
            pages.forEachIndexed { pageIndex, page ->
                NavigationBarItem(
                    icon = {
                        Image(
                            modifier = Modifier.size(28.dp),
                            imageVector = page.icon,
                            contentDescription = stringResource(id = page.title)
                        )
                    },
                    label = { Text(stringResource(id = page.title)) },
                    selected = activePageIndex == pageIndex,
                    onClick = {
                        activePageIndex = pageIndex

                        navController.navigate(page.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

private data class Page(
    val icon: ImageVector,
    @StringRes val title: Int,
    val route: String,
)