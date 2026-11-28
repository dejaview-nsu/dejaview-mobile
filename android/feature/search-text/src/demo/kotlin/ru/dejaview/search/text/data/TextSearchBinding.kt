package ru.dejaview.search.text.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.dejaview.search.text.domain.TextSearch

@Module
@InstallIn(SingletonComponent::class)
internal object TextSearchBinding {
    @Provides fun search(): TextSearch = FakeTextSearch()
}
