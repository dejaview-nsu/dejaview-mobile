package ru.dejaview.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.LoginRoute
import ru.dejaview.navigation.MovieRoute
import ru.dejaview.navigation.ResultsRoute
import ru.dejaview.navigation.SearchHomeRoute
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.presentation.SearchResultsScreen
import ru.dejaview.search.presentation.SearchViewModel

@Composable
internal fun SearchLoginDestination(
    nav: NavHostController,
    entry: NavBackStackEntry,
    route: LoginRoute,
    vm: SearchViewModel,
    services: SearchServices,
) {
    val session by vm.session.collectAsStateWithLifecycle()
    LaunchedEffect(session, route.pendingActionId) {
        if (session is SessionState.Authenticated && nav.currentBackStackEntry == entry) {
            val target = vm.finishLogin(route.pendingActionId)
            nav.popBackStack()
            if (target != null) nav.openSearch(target, vm)
        }
    }
    SearchLoginScreen(
        onCancel = {
            if (nav.currentBackStackEntry == entry) {
                vm.cancelLogin(route.pendingActionId)
                nav.popBackStack()
            }
        },
        onDemoLogin = services.demoLogin,
    )
}

@Composable
internal fun SearchResultsDestination(
    nav: NavHostController,
    entry: NavBackStackEntry,
    route: ResultsRoute,
    vm: SearchViewModel,
) {
    val session by vm.session.collectAsStateWithLifecycle()
    key(route.requestId, session) {
        val result by produceState<RestoredResult?>(initialValue = null) {
            value = RestoredResult(vm.result(route.requestId))
        }
        SearchResultsScreen(
            completion = result?.completion,
            restoring = result == null,
            onReturn = {
                if (nav.isActive(entry)) {
                    if (nav.previousBackStackEntry?.destination?.hasRoute<SearchRoute>() == true) {
                        nav.popBackStack()
                    } else {
                        nav.popBackStack<SearchHomeRoute>(inclusive = false)
                    }
                }
            },
            onMovie = { movieId ->
                if (nav.isActive(entry)) nav.navigate(MovieRoute(movieId))
            },
        )
    }
}

private data class RestoredResult(
    val completion: SearchCompletion?,
)
