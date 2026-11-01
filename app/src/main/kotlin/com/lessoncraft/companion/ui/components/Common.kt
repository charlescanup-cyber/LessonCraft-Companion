package com.lessoncraft.companion.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lessoncraft.companion.BuildConfig
import com.lessoncraft.engine.GateResult

/** A compact selector: a labelled button that opens a list in a dialog, so long lists are picked with one thumb. */
@Composable
fun <T> ChoiceButton(label: String, value: String, options: List<T>, optionLabel: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { open = true },
        modifier = modifier.heightIn(min = 56.dp).semantics { contentDescription = "$label: $value. Tap to change." },
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(options) { opt ->
                        val text = optionLabel(opt)
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.RadioButton) { onSelect(opt); open = false }.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text, style = MaterialTheme.typography.bodyLarge, color = if (text == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        )
    }
}

/** The prompt shown when Print or Export is tapped on the free plan: what it is, and a hand-off to the web app's Bundle page in the browser. */
@Composable
fun BundlePrompt(locked: GateResult.Locked, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(locked.title) },
        text = { Text(locked.message) },
        confirmButton = {
            TextButton(onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.BUNDLE_URL))) }
                onDismiss()
            }) { Text(locked.actionLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}
