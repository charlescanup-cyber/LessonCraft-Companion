package com.lessoncraft.companion.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessoncraft.companion.data.FolderEntity
import com.lessoncraft.engine.LibraryFilter
import com.lessoncraft.engine.Tier

/** Saved lessons: search, favorites, and your own folders. Everything here works with no connection and on every plan. */
@Composable
fun LibraryScreen(vm: LibraryViewModel, onOpen: (Long) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var managing by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Library", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = ui.query,
                onValueChange = vm::setQuery,
                label = { Text("Search lessons") },
                placeholder = { Text("title, standard, topic, tag or text") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { if (ui.query.isNotEmpty()) IconButton(onClick = { vm.setQuery("") }) { Icon(Icons.Default.Close, contentDescription = "Clear search") } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Chip("All", ui.filter == LibraryFilter.All) { vm.setFilter(LibraryFilter.All) } }
                item { Chip("★ Favorites", ui.filter == LibraryFilter.Favorites) { vm.setFilter(LibraryFilter.Favorites) } }
                item { Chip("Unfiled", ui.filter == LibraryFilter.Unfiled) { vm.setFilter(LibraryFilter.Unfiled) } }
                items(ui.folders) { f -> Chip(f.name, (ui.filter as? LibraryFilter.Folder)?.id == f.id) { vm.setFilter(LibraryFilter.Folder(f.id)) } }
                item { Chip("Folders…", false) { managing = true } }
            }
            Text(
                if (ui.tier == Tier.BUNDLE) "Bundle plan: printing and export are on." else "Free plan: printing and export are part of the web app Bundle.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (ui.hits.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (ui.total == 0) "No saved lessons yet. Build one from Create." else "Nothing matches.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ui.hits, key = { it.doc.id }) { hit ->
                    val d = hit.doc
                    Card(Modifier.fillMaxWidth().clickable { onOpen(d.id) }) {
                        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(d.title, style = MaterialTheme.typography.titleMedium)
                                Text("${d.grade} · ${d.subject}${if (d.standardCodes.isNotBlank()) " · " + d.standardCodes.substringBefore(' ') else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (hit.matched.isNotEmpty()) Text("Matched: ${hit.matched.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { vm.toggleFavorite(d.id, d.favorite) },
                                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = if (d.favorite) "Remove from favorites" else "Add to favorites" },
                            ) { Text(if (d.favorite) "★" else "☆", style = MaterialTheme.typography.headlineSmall) }
                        }
                    }
                }
            }
        }
    }

    if (managing) FolderManager(ui.folders, onCreate = vm::createFolder, onRename = vm::renameFolder, onDelete = { vm.deleteFolder(it) }, onClose = { managing = false })
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = Modifier.heightIn(min = 40.dp))
}

/** Make, rename and delete folders. Deleting a folder keeps its lessons: they move to Unfiled. */
@Composable
fun FolderManager(folders: List<FolderEntity>, onCreate: (String) -> Unit, onRename: (Long, String) -> Unit, onDelete: (Long) -> Unit, onClose: () -> Unit) {
    var newName by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf<FolderEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Folders") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = newName, onValueChange = { newName = it.take(40) }, label = { Text("New folder") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { onCreate(newName); newName = "" }, enabled = newName.isNotBlank()) { Text("Create folder") }
                if (folders.isEmpty()) Text("No folders yet.", style = MaterialTheme.typography.bodySmall)
                folders.forEach { f ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(f.name, Modifier.weight(1f).clickable { renaming = f; renameText = f.name }, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { onDelete(f.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete folder ${f.name}") }
                    }
                }
                if (folders.isNotEmpty()) Text("Tap a name to rename it. Deleting a folder keeps its lessons.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Done") } },
    )
    renaming?.let { f ->
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Rename folder") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it.take(40) }, singleLine = true) },
            confirmButton = { TextButton(onClick = { onRename(f.id, renameText); renaming = null }, enabled = renameText.isNotBlank()) { Text("Save") } },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text("Cancel") } },
        )
    }
}
