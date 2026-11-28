package ru.dejaview.auth

import androidx.compose.runtime.Composable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.dejaview.auth.data.SessionGateway
import ru.dejaview.auth.data.SessionRepository
import ru.dejaview.auth.domain.LoginService
import ru.dejaview.auth.presentation.LoginScreen
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.contracts.session.SessionCookieStore
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.core.ui.auth.LoginUi
import ru.dejaview.security.SessionRegistry
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AuthModule {
    @Provides @Singleton
    fun repository(
        registry: SessionRegistry,
        gateway: SessionGateway,
    ): SessionRepository = SessionRepository(registry, gateway, backendOrigin)

    @Provides fun loginUi(service: LoginService): LoginUi =
        object : LoginUi {
            @Composable
            override fun Content(onCancel: () -> Unit) {
                LoginScreen(service, onCancel)
            }
        }

    @Provides fun controller(repository: SessionRepository): SessionController = repository

    @Provides fun login(repository: SessionRepository): LoginService = repository

    @Provides fun reader(registry: SessionRegistry): SessionReader = registry

    @Provides fun cookies(registry: SessionRegistry): SessionCookieStore = registry
}
