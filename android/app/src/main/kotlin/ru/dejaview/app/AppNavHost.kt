package ru.dejaview.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.dejaview.auth.presentation.LoginScreen
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.core.ui.theme.DejaViewSpacing
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
    services: SessionServices,
    navController: NavHostController = rememberNavController(),
) {
    val session by services.reader.state.collectAsStateWithLifecycle()
    val actions = viewModel<SessionActions> { SessionActions(services) }
    val notice by actions.notice.collectAsStateWithLifecycle()
    val account = session as? SessionState.Authenticated
    LaunchedEffect(account?.accountScope) { if (account == null) actions.forgetPrivateData() }
    Scaffold { insets ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets)
                .padding(horizontal = DejaViewSpacing.screen),
        ) {
            val badge = stringResource(R.string.demo_badge)
            if (badge.isNotEmpty()) Text(badge)
            notice?.let {
                Text(stringResource(it))
                TextButton(onClick = actions::dismiss) { Text(stringResource(R.string.notice_close)) }
            }

            NavHost(navController = navController, startDestination = SearchRoute(SearchMode.TEXT)) {
                composable<SearchRoute> { entry ->
                    val route = entry.toRoute<SearchRoute>()
                    val mode = searchModes[route.mode]
                    Column {
                        if (route.mode == SearchMode.TEXT) {
                            Row {
                                TextButton(enabled = session != SessionState.Initializing, onClick = {
                                    val audio = SearchRoute(SearchMode.AUDIO)
                                    if (account == null) {
                                        navController.navigate(LoginRoute(actions.expect(audio)))
                                    } else {
                                        navController.navigate(audio)
                                    }
                                }) { Text(stringResource(R.string.open_audio)) }
                                TextButton(
                                    onClick = { navController.navigate(ProfileRoute) },
                                ) { Text(stringResource(R.string.profile_placeholder)) }
                            }
                        }
                        if (route.mode == SearchMode.AUDIO && account == null) {
                            Text(stringResource(R.string.audio_requires_login))
                            TextButton(enabled = session != SessionState.Initializing, onClick = {
                                navController.navigate(LoginRoute(actions.expect(route)))
                            }) { Text(stringResource(R.string.open_login)) }
                        } else if (mode == null) {
                            SearchFallback(route)
                        } else {
                            mode.Content(
                                draftId = route.draftId,
                                onCompleted = { completion ->
                                    if (navController.currentBackStackEntry == entry) {
                                        if (route.mode == SearchMode.AUDIO) {
                                            if (account == null || services.reader.state.value != account) return@Content
                                            actions.completed(account.accountScope, completion)
                                        }
                                        navController.navigate(ResultsRoute(completion.requestId))
                                    }
                                },
                                modifier = Modifier,
                            )
                        }
                    }
                }
                composable<ResultsRoute> { entry ->
                    Column {
                        Text(stringResource(R.string.results_placeholder))
                        val result = actions.result(account?.accountScope, entry.toRoute<ResultsRoute>().requestId)
                        if (result?.results?.isEmpty() == true) Text(stringResource(R.string.audio_empty))
                        result?.results?.forEach { movie ->
                            TextButton(onClick = { navController.navigate(MovieRoute(movie.movieId)) }) { Text(movie.title) }
                        }
                    }
                }
                composable<MovieRoute> { Text(stringResource(R.string.movie_placeholder)) }
                composable<LoginRoute> { entry ->
                    val id = entry.toRoute<LoginRoute>().pendingActionId
                    val cancel: () -> Unit = {
                        actions.consume(id)
                        navController.popBackStack()
                        Unit
                    }
                    BackHandler(onBack = cancel)
                    LoginScreen(services.login, onSuccess = {
                        if (navController.currentBackStackEntry == entry) {
                            val destination = actions.consume(id)
                            navController.popBackStack()
                            destination?.let {
                                val previous = navController.currentBackStackEntry
                                if (previous?.destination?.hasRoute<SearchRoute>() != true || previous.toRoute<SearchRoute>() != it) {
                                    navController.navigate(it)
                                }
                            }
                        }
                    }, onCancel = cancel)
                }
                composable<LibraryRoute> { Text(stringResource(R.string.library_placeholder)) }
                composable<ProfileRoute> {
                    Column {
                        Text(stringResource(R.string.profile_placeholder))
                        if (account == null) {
                            TextButton(enabled = session != SessionState.Initializing, onClick = { navController.navigate(LoginRoute()) }) {
                                Text(stringResource(R.string.open_login))
                            }
                        } else {
                            Text(account.username)
                            TextButton(onClick = {
                                actions.logout()
                                navController.navigate(SearchRoute(SearchMode.TEXT)) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }) { Text(stringResource(R.string.logout)) }
                        }
                    }
                }
                composable<HistoryRoute> { Text(stringResource(R.string.history_placeholder)) }
            }
        }
    }
}
