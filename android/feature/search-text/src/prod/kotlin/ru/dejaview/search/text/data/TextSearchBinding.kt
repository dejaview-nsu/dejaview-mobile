package ru.dejaview.search.text.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.dejaview.search.text.domain.TextSearch

@Module
@InstallIn(SingletonComponent::class)
internal interface TextSearchBinding {
    @Binds fun bind(search: HttpTextSearch): TextSearch
}
