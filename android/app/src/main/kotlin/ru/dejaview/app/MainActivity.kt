package ru.dejaview.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        setContent {
            DejaViewTheme {
                val preferences = getSharedPreferences(STARTUP_PREFERENCES, MODE_PRIVATE)
                AppNavHost(
                    searchModes = searchModes,
                    searchServices = searchServices,
                    controller = sessions.controller,
                    showIntroduction = !preferences.getBoolean(INTRODUCTION_COMPLETED, false),
                    onIntroductionCompleted = { preferences.edit().putBoolean(INTRODUCTION_COMPLETED, true).apply() },
                )
            }
        }
    }
}

internal const val STARTUP_PREFERENCES = "app-startup"
internal const val INTRODUCTION_COMPLETED = "introduction-completed"
