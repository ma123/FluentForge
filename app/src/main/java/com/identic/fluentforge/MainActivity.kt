package com.identic.fluentforge

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import coil.annotation.ExperimentalCoilApi
import com.identic.fluentforge.dataReader.remote.utils.NetworkObserver
import com.identic.fluentforge.service.SimpleMediaService
import com.identic.fluentforge.ui.navigation.Screens
import com.identic.fluentforge.ui.screens.main.MainScreen
import com.identic.fluentforge.ui.screens.viewmodels.SettingsViewModel
import com.identic.fluentforge.ui.screens.viewmodels.ThemeMode
import com.identic.fluentforge.ui.theme.FluentForgeTheme
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var networkObserver: NetworkObserver
    lateinit var settingsViewModel: SettingsViewModel
    private lateinit var mainViewModel: MainViewModel

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

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        startDestination = Screens.SpeakScreen.route,
                        networkStatus = status,
                        settingsViewModel = settingsViewModel,
                        startService = ::startService
                    )
                }
            }
        }

        checkPermission()
    }


    fun checkPermission(): Boolean {
        return if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, RECORD_AUDIO),
                1
            )
            false
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
}