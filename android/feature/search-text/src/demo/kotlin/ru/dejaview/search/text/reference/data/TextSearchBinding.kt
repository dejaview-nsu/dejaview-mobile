package ru.dejaview.search.text.reference.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.dejaview.search.text.reference.domain.ReferenceSearch

@Module
@InstallIn(SingletonComponent::class)
internal object TextSearchBinding {
    @Provides fun search(): ReferenceSearch = FakeReferenceSearch()
}
