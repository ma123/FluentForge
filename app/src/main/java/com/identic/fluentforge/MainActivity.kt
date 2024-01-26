package com.identic.fluentforge

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.identic.fluentforge.reader.ui.navigation.NavGraph
import com.identic.fluentforge.reader.ui.navigation.Screens
import com.identic.fluentforge.reader.ui.screens.settings.viewmodels.SettingsViewModel
import com.identic.fluentforge.reader.ui.screens.settings.viewmodels.ThemeMode
import com.identic.fluentforge.reader.utils.NetworkObserver
import com.identic.fluentforge.service.SimpleMediaService
import com.identic.fluentforge.ui.screens.RadioScreen
import com.identic.fluentforge.ui.screens.SpeechScreen
import com.identic.fluentforge.ui.theme.FluentForgeTheme
import dagger.hilt.android.AndroidEntryPoint

private data class Page(
    val iconRes: ImageVector,
    @StringRes val stringRes: Int,
)

private val pages = listOf(
    Page(iconRes = Icons.Filled.RecordVoiceOver, stringRes = R.string.title_speak),
    Page(iconRes = Icons.Filled.MenuBook, stringRes = R.string.title_library),
    Page(iconRes = Icons.Filled.Radio, stringRes = R.string.title_radio),
)

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var networkObserver: NetworkObserver
    lateinit var settingsViewModel: SettingsViewModel
    private lateinit var mainViewModel: MainViewModel

   // var speechInput = mutableStateOf("")

    private var isServiceRunning = false

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        networkObserver = NetworkObserver(applicationContext)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        ThemeMode.entries.find { it.ordinal == settingsViewModel.getThemeValue() }
            ?.let { settingsViewModel.setTheme(it) }
        settingsViewModel.setMaterialYou(settingsViewModel.getMaterialYouValue())

        // Install splash screen before setting content.
        installSplashScreen().setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }

        setContent {
            FluentForgeTheme(settingsViewModel = settingsViewModel) {

                val status by networkObserver.observe().collectAsState(
                    initial = NetworkObserver.Status.Unavailable
                )

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var activePageIndex by rememberSaveable { mutableIntStateOf(0) }

                    BackHandler(enabled = activePageIndex != 0) {
                        activePageIndex = 0
                    }

                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            AnimatedContent(targetState = activePageIndex, label = "") {
                                when (it) {
                                    0 -> {
                                        SpeechScreen()
                                    }

                                    1 -> {
                                        val navController = rememberNavController()

                                        NavGraph(
                                            startDestination = Screens.HomeScreen.route,
                                            navController = navController,
                                            networkStatus = status
                                        )
                                    }

                                    2 -> {
                                        RadioScreen(startService = ::startService)
                                    }
                                }
                            }
                        }
                        NavigationBar {
                            pages.forEachIndexed { pageIndex, page ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = page.iconRes,
                                            contentDescription = stringResource(id = page.stringRes)
                                        )
                                    },
                                    label = { Text(stringResource(id = page.stringRes)) },
                                    selected = activePageIndex == pageIndex,
                                    onClick = {
                                        activePageIndex = pageIndex
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        checkPermission()
    }


    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, RECORD_AUDIO), 1
                ); false
            }
        } else {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, SimpleMediaService::class.java))
        isServiceRunning = false
    }

    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, SimpleMediaService::class.java)
            startForegroundService(intent)
            isServiceRunning = true
        }
    }

   /* fun askSpeechInput(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech not Available", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk")
            startActivityForResult(intent, 102)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            speechInput.value = result?.get(0).toString()
        }
    }*/
}