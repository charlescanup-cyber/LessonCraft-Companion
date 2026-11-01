package com.lessoncraft.companion.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessoncraft.companion.AppContainer
import com.lessoncraft.companion.data.FolderEntity
import com.lessoncraft.companion.data.LessonEntity
import com.lessoncraft.companion.data.decode
import com.lessoncraft.engine.GateResult
import com.lessoncraft.engine.GatedAction
import com.lessoncraft.engine.Gate
import com.lessoncraft.engine.Lesson
import com.lessoncraft.engine.Tier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUi(val entity: LessonEntity? = null, val lesson: Lesson? = null, val folders: List<FolderEntity> = emptyList(), val tier: Tier = Tier.FREE, val loaded: Boolean = false)

class DetailViewModel(private val container: AppContainer, private val id: Long) : ViewModel() {
    val ui: StateFlow<DetailUi> = combine(container.repository.observe(id), container.repository.folders, container.entitlements.tier) { e, folders, tier ->
        DetailUi(e, e?.let { runCatching { it.decode() }.getOrNull() }, folders, tier, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUi())

    fun toggleFavorite() { ui.value.entity?.let { e -> viewModelScope.launch { container.repository.setFavorite(id, !e.favorite) } } }
    fun move(folderId: Long?) = viewModelScope.launch { container.repository.move(id, folderId) }
    fun delete(onDone: () -> Unit) = viewModelScope.launch { container.repository.delete(id); onDone() }

    /** May this plan print or export? The answer is a decision from the engine, so every screen asks the same rule. */
    fun gate(action: GatedAction): GateResult = Gate.check(action, ui.value.tier)
}
