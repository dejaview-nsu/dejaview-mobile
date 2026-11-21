package ru.dejaview.app

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import ru.dejaview.model.SearchMode
import ru.dejaview.search.api.SearchModalityUi

@Module
@InstallIn(SingletonComponent::class)
interface SearchModeBindings {
    @Multibinds
    fun searchModes(): Map<SearchMode, SearchModalityUi>
}
