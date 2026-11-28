package ru.dejaview.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.HistoryRoute
import ru.dejaview.navigation.LibraryRoute
import ru.dejaview.navigation.LoginRoute
import ru.dejaview.navigation.MovieRoute
import ru.dejaview.navigation.OnboardingRoute
import ru.dejaview.navigation.OpeningRoute
import ru.dejaview.navigation.ProfileRoute
import ru.dejaview.navigation.RegistrationRoute
import ru.dejaview.navigation.ResultsRoute
import ru.dejaview.navigation.SearchAccessRoute
import ru.dejaview.navigation.SearchHomeRoute
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.api.SearchModalityUi

@Composable
internal fun AppNavHost(
    searchModes: Map<SearchMode, SearchModalityUi>,
    searchServices: SearchServices,
    controller: SessionController,
    navController: NavHostController = rememberNavController(),
    showIntroduction: Boolean = false,
    onIntroductionCompleted: () -> Unit = {},
) {
    val actions = viewModel<SessionActions> { SessionActions(controller) }
    val notice by actions.notice.collectAsStateWithLifecycle()
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
            val startDestination = if (showIntroduction) OpeningRoute else SearchHomeRoute
            NavHost(navController = navController, startDestination = startDestination) {
                composable<OpeningRoute> { entry ->
                    OpeningSplash {
                        if (navController.currentBackStackEntry == entry) {
                            navController.navigate(OnboardingRoute) { popUpTo<OpeningRoute> { inclusive = true } }
                        }
                    }
                }
                composable<OnboardingRoute> { entry ->
                    OnboardingScreen {
                        if (navController.isActive(entry)) {
                            onIntroductionCompleted()
                            navController.navigate(SearchHomeRoute) { popUpTo<OnboardingRoute> { inclusive = true } }
                            navController.navigate(LoginRoute())
                        }
                    }
                }
                composable<SearchHomeRoute> { entry ->
                    val vm = searchViewModel(searchServices, navController, entry)
                    val session by vm.session.collectAsStateWithLifecycle()
                    SearchHome(
                        guest = session == SessionState.Guest || session == SessionState.Expired,
                        onProfile = { if (navController.isActive(entry)) navController.navigate(ProfileRoute) },
                        onLogin = {
                            if (navController.isActive(entry)) navController.navigate(LoginRoute())
                        },
                    ) { mode ->
                        if (navController.isActive(entry)) {
                            if (vm.canOpen(mode)) navController.openSearch(SearchRoute(mode), vm)
                            else navController.navigate(SearchAccessRoute(mode))
                        }
                    }
                }
                composable<SearchAccessRoute> { entry ->
                    val route = entry.toRoute<SearchAccessRoute>()
                    val vm = searchViewModel(searchServices, navController, entry)
                    val proceed = { mode: SearchMode, register: Boolean ->
                        if (navController.isActive(entry)) {
                            navController.popBackStack()
                            navController.openSearch(SearchRoute(mode), vm)
                            if (register && !vm.canOpen(mode) && searchServices.login != null) {
                                navController.navigate(RegistrationRoute)
                            }
                        }
                    }
                    SearchAccessScreen(
                        route.mode,
                        onLogin = { proceed(route.mode, false) },
                        onRegister = { proceed(route.mode, true) },
                        onTextSearch = { proceed(SearchMode.TEXT, false) },
                    )
                }
                composable<SearchRoute> { entry ->
                    val route = entry.toRoute<SearchRoute>()
                    val vm = searchViewModel(searchServices, navController, entry)
                    SearchInputDestination(navController, entry, route, searchModes[route.mode], vm)
                }
                composable<ResultsRoute> { entry ->
                    val vm = searchViewModel(searchServices, navController, entry)
                    SearchResultsDestination(navController, entry, entry.toRoute(), vm)
                }
                composable<MovieRoute> { Text(stringResource(R.string.movie_placeholder)) }
                composable<LoginRoute> { entry ->
                    val vm = searchViewModel(searchServices, navController, entry)
                    SearchLoginDestination(navController, entry, entry.toRoute(), vm, searchServices)
                }
                composable<RegistrationRoute> { entry ->
                    searchServices.login?.Registration {
                        if (navController.isActive(entry)) navController.popBackStack()
                    }
                }
                composable<LibraryRoute> { Text(stringResource(R.string.library_placeholder)) }
                composable<ProfileRoute> {
                    val session by searchServices.session.state.collectAsStateWithLifecycle()
                    Text(stringResource(R.string.profile_placeholder))
                    (session as? SessionState.Authenticated)?.let { account ->
                        Text(account.username)
                        TextButton(onClick = {
                            actions.logout()
                            navController.popBackStack<SearchHomeRoute>(inclusive = false)
                        }) { Text(stringResource(R.string.logout)) }
                    }
                }
                composable<HistoryRoute> { Text(stringResource(R.string.history_placeholder)) }
            }
        }
    }
}
