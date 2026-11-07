package com.lessoncraft.companion.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessoncraft.companion.AppContainer
import com.lessoncraft.engine.Grade
import com.lessoncraft.engine.Idea
import com.lessoncraft.engine.IdeaEngine
import com.lessoncraft.engine.LessonBuilder
import com.lessoncraft.engine.LessonRequest
import com.lessoncraft.engine.SeenIdea
import com.lessoncraft.engine.StandardResolver
import com.lessoncraft.engine.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val STANDARD_MAX = 300
const val CUSTOM_MAX = 1500
const val BOOK_MAX = 120

data class CreateUi(
    val grade: Grade = Grade.G4,
    val subject: Subject = Subject.MATH,
    /** Compact structured input: a standard code or core topic. */
    val standardInput: String = "",
    /** Free-form conversational layer: a lesson vision, theme or student needs. */
    val customPrompt: String = "",
    /** Optional: the book or novel the lesson is built on. */
    val bookTitle: String = "",
    /** Optional curriculum code (e.g. NGSS HS-LS1-4), kept separate from the topic box like the web app. */
    val standardCode: String = "",
    val ideas: List<Idea> = emptyList(),
    val rounds: Int = 0,
    /** What the standard box was understood as ("Matched RL.1.2"), shown under the field. */
    val understood: String? = null,
    val error: String? = null,
    val busy: Boolean = false,
) {
    /** Either box, or both, is enough. */
    val canSubmit: Boolean get() = standardInput.isNotBlank() || customPrompt.isNotBlank() || bookTitle.isNotBlank() || standardCode.isNotBlank()
}

/**
 * The create flow's state. Ideas come from the on-device rule engine, so there is no waiting on a network: the "busy" flag only covers the few
 * milliseconds the engine takes. The session's idea history lives here and is passed back to the engine on every "New ideas".
 */
class CreateViewModel(private val container: AppContainer) : ViewModel() {
    private val _ui = MutableStateFlow(CreateUi())
    val ui: StateFlow<CreateUi> = _ui.asStateFlow()
    private var seen: List<SeenIdea> = emptyList()

    fun setGrade(g: Grade) = _ui.update { it.copy(grade = g, understood = null) }
    fun setSubject(s: Subject) = _ui.update { it.copy(subject = s, understood = null) }
    fun setStandard(v: String) = _ui.update { it.copy(standardInput = v.take(STANDARD_MAX), understood = null, error = null) }
    fun setBook(v: String) = _ui.update { it.copy(bookTitle = v.take(BOOK_MAX), understood = null, error = null) }
    fun setStandardCode(v: String) = _ui.update { it.copy(standardCode = v.take(STANDARD_MAX), understood = null, error = null) }
    fun setCustom(v: String) = _ui.update { it.copy(customPrompt = v.take(CUSTOM_MAX), error = null) }

    private fun request(s: CreateUi) = LessonRequest(s.grade, s.subject, s.standardInput.trim(), s.customPrompt.trim(), s.bookTitle.trim(), s.standardCode.trim())

    /** Three ideas. `again` keeps the history so the new round differs from every earlier one; a fresh start clears it. */
    fun getIdeas(again: Boolean, onReady: () -> Unit = {}) {
        val s = _ui.value
        if (!s.canSubmit || s.busy) return
        if (!again) seen = emptyList()
        _ui.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                runCatching {
                    val r = request(s)
                    val res = StandardResolver.resolve(r)
                    res to IdeaEngine.generate(r, res, seen)
                }
            }
            result.onSuccess { (res, ideas) ->
                seen = (seen + ideas.map { SeenIdea(it.title, it.hook, it.summary, it.lens) }).takeLast(30)
                val std = res.standard?.let { "${if (res.exact) "Standard" else "Closest standard"}: ${it.georgiaCode} (${it.shortCode})" }
                val note = listOfNotNull(res.book?.let { "Book: ${it.describe()}${if (it.known) "" else " (not in the built-in list: add your own chapters)"}" }, std).joinToString("\n").ifEmpty { null }
                _ui.update { it.copy(ideas = ideas, rounds = if (again) it.rounds + 1 else 1, understood = note, busy = false) }
                onReady()
            }.onFailure { e -> _ui.update { it.copy(busy = false, error = e.message ?: "Something went wrong.") } }
        }
    }

    /** Build the lesson from the picked idea, save it to the library, and hand back its id. */
    fun build(idea: Idea, onSaved: (Long) -> Unit) {
        val s = _ui.value
        if (s.busy) return
        _ui.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    val r = request(s)
                    LessonBuilder.build(r, StandardResolver.resolve(r), idea)
                }.let { container.repository.save(it) }
            }.onSuccess { id ->
                _ui.update { it.copy(busy = false) }
                onSaved(id)
            }.onFailure { e -> _ui.update { it.copy(busy = false, error = e.message ?: "The lesson could not be saved.") } }
        }
    }
}
