package com.lessoncraft.companion.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessoncraft.companion.share.PrintExport
import com.lessoncraft.companion.ui.components.BundlePrompt
import com.lessoncraft.engine.GateResult
import com.lessoncraft.engine.GatedAction
import com.lessoncraft.engine.ItemKind
import com.lessoncraft.engine.Lesson
import com.lessoncraft.engine.Visual

/**
 * One saved lesson: favorite it, file it in a folder, read it. Print and Export are the two gated actions: on the free plan they open the Bundle
 * prompt; on the Bundle plan they print or share the lesson. Reading, favorites and folders never depend on the plan.
 */
@Composable
fun LessonDetailScreen(vm: DetailViewModel, onClosed: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var locked by remember { mutableStateOf<GateResult.Locked?>(null) }
    var picking by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var answers by remember { mutableStateOf(false) }

    val lesson = ui.lesson
    val entity = ui.entity
    if (lesson == null || entity == null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text(if (ui.loaded) "This lesson is no longer saved." else "Loading…", style = MaterialTheme.typography.bodyLarge)
            if (ui.loaded) TextButton(onClick = onClosed) { Text("Back to the library") }
        }
        return
    }

    fun gated(action: GatedAction, run: () -> Unit) {
        when (val r = vm.gate(action)) {
            GateResult.Allowed -> run()
            is GateResult.Locked -> locked = r
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)
        Text("${lesson.grade.label} · ${lesson.subject.label} · ${lesson.totalMinutes} min · ${lesson.standardLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = vm::toggleFavorite, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(if (entity.favorite) "★ Favorite" else "☆ Favorite") }
            OutlinedButton(onClick = { picking = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text(ui.folders.firstOrNull { it.id == entity.folderId }?.name ?: "Move to folder", maxLines = 1)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { gated(GatedAction.PRINT) { PrintExport.print(context, lesson) } }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Print") }
            OutlinedButton(onClick = { gated(GatedAction.EXPORT) { PrintExport.export(context, lesson) } }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Export") }
        }

        Section("Objective (${lesson.objectiveBloom.label})") { Text(lesson.objective) }
        if (lesson.customNotes.isNotBlank()) Section("Your instructions") { Text(lesson.customNotes) }
        Section("Materials") { lesson.materials.forEach { Text("• $it") } }
        Section("Instructional sequence") {
            lesson.sequence.forEach { s ->
                Text("${s.phase} · ${s.minutes} min · ${s.bloom.label}", style = MaterialTheme.typography.titleSmall)
                Text(s.title, style = MaterialTheme.typography.bodyLarge)
                Text(s.description)
                Text("Engagement: ${s.udl.engagement}", style = MaterialTheme.typography.bodySmall)
                Text("Representation: ${s.udl.representation}", style = MaterialTheme.typography.bodySmall)
                Text("Action & expression: ${s.udl.actionExpression}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
            }
        }
        Section("Student handout") { lesson.items.forEach { ItemView(it, answers) } }
        OutlinedButton(onClick = { answers = !answers }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (answers) "Hide answer key" else "Show answer key") }
        Section("Differentiation") {
            Text("IEP / 504: ${lesson.differentiation.iep504}")
            Text("On level: ${lesson.differentiation.onLevel}")
            Text("Enrichment: ${lesson.differentiation.enrichment}")
        }
        OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Delete lesson") }
    }

    locked?.let { BundlePrompt(it) { locked = null } }

    if (picking) {
        AlertDialog(
            onDismissRequest = { picking = false },
            title = { Text("Move to folder") },
            text = {
                Column {
                    TextButton(onClick = { vm.move(null); picking = false }) { Text("Unfiled") }
                    ui.folders.forEach { f -> TextButton(onClick = { vm.move(f.id); picking = false }) { Text(f.name) } }
                    if (ui.folders.isEmpty()) Text("Make folders from the Library tab.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this lesson?") },
            text = { Text("It will be removed from this device. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onClosed) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
private fun ItemView(item: com.lessoncraft.engine.StudentItem, showAnswer: Boolean) {
    Column(Modifier.padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item.teacherScript?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary) }
        Text("${item.number}. ${item.prompt}", style = MaterialTheme.typography.bodyLarge)
        (item.visual as? Visual.Dots)?.let { Text("● ".repeat(it.count).trim(), style = MaterialTheme.typography.headlineSmall) }
        if (item.kind == ItemKind.DRAW_BOX) Text("[ drawing box ]", style = MaterialTheme.typography.bodySmall)
        Text(item.bloom.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (showAnswer) Text("Answer: ${item.answer}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}

