package ru.dejaview.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.HistoryRoute
import ru.dejaview.navigation.LibraryRoute
import ru.dejaview.navigation.LoginRoute
import ru.dejaview.navigation.MovieRoute
import ru.dejaview.navigation.ProfileRoute
import ru.dejaview.navigation.ResultsRoute
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.api.SearchModalityUi

@Composable
internal fun AppNavHost(
    searchModes: Map<SearchMode, SearchModalityUi>,
    navController: NavHostController = rememberNavController(),
) {
    Scaffold { insets ->
        Column(Modifier.fillMaxSize().padding(insets).padding(16.dp)) {
            val badge = stringResource(R.string.demo_badge)
            if (badge.isNotEmpty()) Text(badge)

            NavHost(navController = navController, startDestination = SearchRoute(SearchMode.TEXT)) {
                composable<SearchRoute> { entry ->
                    val route = entry.toRoute<SearchRoute>()
                    val mode = searchModes[route.mode]
                    if (mode == null) {
                        Text(stringResource(R.string.search_placeholder))
                    } else {
                        mode.Content(
                            draftId = route.draftId,
                            onCompleted = { completion ->
                                if (navController.currentBackStackEntry == entry) {
                                    navController.navigate(ResultsRoute(completion.requestId))
                                }
                            },
                            modifier = Modifier,
                        )
                    }
                }
                composable<ResultsRoute> { Text(stringResource(R.string.results_placeholder)) }
                composable<MovieRoute> { Text(stringResource(R.string.movie_placeholder)) }
                composable<LoginRoute> { Text(stringResource(R.string.login_placeholder)) }
                composable<LibraryRoute> { Text(stringResource(R.string.library_placeholder)) }
                composable<ProfileRoute> { Text(stringResource(R.string.profile_placeholder)) }
                composable<HistoryRoute> { Text(stringResource(R.string.history_placeholder)) }
            }
        }
    }
}
