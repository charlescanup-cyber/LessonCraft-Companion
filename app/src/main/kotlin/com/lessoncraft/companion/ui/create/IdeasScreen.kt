package com.lessoncraft.companion.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Three ideas as cards. "New ideas" draws a round built on ground the teacher has not seen, and "Build this lesson" saves a finished lesson. */
@Composable
fun IdeasScreen(vm: CreateViewModel, onBack: () -> Unit, onLesson: (Long) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("Round ${ui.rounds}", style = MaterialTheme.typography.titleMedium)
                Text(ui.understood ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(ui.ideas) { idea ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(idea.title, style = MaterialTheme.typography.titleLarge)
                        Text(idea.summary, style = MaterialTheme.typography.bodyMedium)
                        Text("Hook: ${idea.hook}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { vm.build(idea, onLesson) }, enabled = !ui.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Build this lesson") }
                    }
                }
            }
            ui.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.getIdeas(again = true) }, enabled = !ui.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("New ideas") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Back to the form") }
        }
    }
}
