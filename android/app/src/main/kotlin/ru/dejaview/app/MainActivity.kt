package ru.dejaview.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.saveable.rememberSaveable
import dagger.hilt.android.AndroidEntryPoint
import ru.dejaview.core.ui.theme.DejaViewTheme
import ru.dejaview.model.SearchMode
import ru.dejaview.search.api.SearchModalityUi
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var searchModes: Map<SearchMode, @JvmSuppressWildcards SearchModalityUi>

    @Inject
    lateinit var searchServices: SearchServices

    @Inject lateinit var sessions: SessionServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val preferences = getSharedPreferences(STARTUP_PREFERENCES, MODE_PRIVATE)
        val firstLaunch = !preferences.getBoolean(INTRODUCTION_STARTED, false)
        if (firstLaunch) preferences.edit().putBoolean(INTRODUCTION_STARTED, true).apply()
        setContent {
            DejaViewTheme {
                val showIntroduction = rememberSaveable { firstLaunch }
                AppNavHost(
                    searchModes = searchModes,
                    searchServices = searchServices,
                    controller = sessions.controller,
                    showIntroduction = showIntroduction,
                )
            }
        }
    }
}

internal const val STARTUP_PREFERENCES = "app-startup"
internal const val INTRODUCTION_STARTED = "introduction-started"
