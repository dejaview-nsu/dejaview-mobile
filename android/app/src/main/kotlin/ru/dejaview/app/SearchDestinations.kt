package ru.dejaview.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.LoginRoute
import ru.dejaview.navigation.MovieRoute
import ru.dejaview.navigation.ResultsRoute
import ru.dejaview.navigation.SearchHomeRoute
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.api.SearchModalityUi
import ru.dejaview.search.presentation.SearchResultsScreen
import ru.dejaview.search.presentation.SearchViewModel

@Composable
internal fun searchViewModel(
    services: SearchServices,
    nav: NavHostController,
    entry: NavBackStackEntry,
): SearchViewModel {
    val graphEntry = remember(entry) { nav.getBackStackEntry(nav.graph.id) }
    return viewModel(
        viewModelStoreOwner = graphEntry,
        factory = viewModelFactory { initializer { SearchViewModel(services.session, services.history) } },
    )
}

internal fun NavHostController.openSearch(route: SearchRoute, vm: SearchViewModel) {
    val pendingId = vm.requestLogin(route)
    if (pendingId == null) navigate(route) { launchSingleTop = true }
    else navigate(LoginRoute(pendingId))
}

@Composable
internal fun SearchInputDestination(
    nav: NavHostController,
    entry: NavBackStackEntry,
    route: SearchRoute,
    mode: SearchModalityUi?,
    vm: SearchViewModel,
) {
    val session by vm.session.collectAsStateWithLifecycle()
    val sourceSession = session
    key(sourceSession) {
        SearchModeScreen(
            route = route,
            entrypoint = mode,
            canOpen = vm.canOpen(route.mode),
            initializing = sourceSession == SessionState.Initializing,
            onLogin = { nav.openSearch(route, vm) },
            onCompleted = { completion ->
                if (nav.isActive(entry) && vm.complete(completion, route.mode, sourceSession)) {
                    nav.navigate(ResultsRoute(completion.requestId))
                }
            },
        )
    }
}

internal fun NavHostController.isActive(entry: NavBackStackEntry): Boolean =
    currentBackStackEntry == entry && entry.lifecycle.currentState == Lifecycle.State.RESUMED
