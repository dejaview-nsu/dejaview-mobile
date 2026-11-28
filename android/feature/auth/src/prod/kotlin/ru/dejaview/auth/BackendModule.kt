package ru.dejaview.auth

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.dejaview.auth.data.HttpSessionGateway
import ru.dejaview.auth.data.SessionGateway
import ru.dejaview.feature.auth.BuildConfig
import ru.dejaview.network.BackendClient
import ru.dejaview.security.KeystoreSessionVault
import ru.dejaview.security.SessionRegistry
import javax.inject.Singleton

internal val backendOrigin = BuildConfig.API_ORIGIN

@Module
@InstallIn(SingletonComponent::class)
internal object BackendModule {
    @Provides @Singleton
    fun client() = BackendClient(backendOrigin)

    @Provides fun gateway(client: BackendClient): SessionGateway = HttpSessionGateway(client)

    @Provides @Singleton
    fun sessions(
        @ApplicationContext context: Context,
    ) = SessionRegistry(backendOrigin, KeystoreSessionVault(context), CoroutineScope(SupervisorJob() + Dispatchers.IO))
}
