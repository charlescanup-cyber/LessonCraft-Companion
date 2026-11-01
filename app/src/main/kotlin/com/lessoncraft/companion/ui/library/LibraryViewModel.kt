package com.lessoncraft.companion.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessoncraft.companion.AppContainer
import com.lessoncraft.companion.data.FolderEntity
import com.lessoncraft.engine.LessonSearch
import com.lessoncraft.engine.LibraryFilter
import com.lessoncraft.engine.SearchHit
import com.lessoncraft.engine.Tier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUi(
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter.All,
    val hits: List<SearchHit> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val total: Int = 0,
    val tier: Tier = Tier.FREE,
)

/** The library: on-device search over every saved lesson, with favorites and folders as filters. All of it is a local query plus the engine's ranker. */
class LibraryViewModel(private val container: AppContainer) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow<LibraryFilter>(LibraryFilter.All)

    val ui: StateFlow<LibraryUi> = combine(container.repository.searchables, container.repository.folders, query, filter, container.entitlements.tier) { docs, folders, q, f, tier ->
        LibraryUi(q, f, LessonSearch.search(q, docs, f), folders, docs.size, tier)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUi())

    fun setQuery(q: String) { query.value = q }
    fun setFilter(f: LibraryFilter) { filter.update { f } }

    fun toggleFavorite(id: Long, current: Boolean) = viewModelScope.launch { container.repository.setFavorite(id, !current) }
    fun createFolder(name: String) { if (name.isNotBlank()) viewModelScope.launch { container.repository.createFolder(name) } }
    fun renameFolder(id: Long, name: String) { if (name.isNotBlank()) viewModelScope.launch { container.repository.renameFolder(id, name) } }
    fun deleteFolder(id: Long) = viewModelScope.launch {
        container.repository.deleteFolder(id)
        // Looking at a folder that was just deleted would show an empty list forever.
        if ((filter.value as? LibraryFilter.Folder)?.id == id) filter.value = LibraryFilter.All
    }
}
