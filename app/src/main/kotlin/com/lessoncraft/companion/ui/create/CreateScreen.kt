package com.lessoncraft.companion.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessoncraft.engine.Grade
import com.lessoncraft.engine.Subject

/**
 * The generation screen. Class details are compact selectors at the top; below them, the dual input: a one-line "Topic / standard" and a large
 * conversational "Custom instructions" box. The action bar sits at the bottom, in reach of a thumb and above the keyboard.
 */
@Composable
fun CreateScreen(vm: CreateViewModel, onIdeas: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("New lesson", style = MaterialTheme.typography.headlineMedium)
            Text("Plans are built on this device. Nothing is sent anywhere.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.lessoncraft.companion.ui.components.ChoiceButton("Grade", ui.grade.label, Grade.entries, { it.label }, vm::setGrade, Modifier.weight(1f))
                com.lessoncraft.companion.ui.components.ChoiceButton("Subject", ui.subject.label, Subject.entries, { it.label }, vm::setSubject, Modifier.weight(1f))
            }

            // Compact, structured: a standard code or a core topic.
            OutlinedTextField(
                value = ui.standardInput,
                onValueChange = vm::setStandard,
                label = { Text("Topic / standard") },
                placeholder = { Text("ELAGSE1RL2, RL.1.2 or Comparing fractions") },
                supportingText = { Text(ui.understood ?: "A standard code or a core topic.") },
                singleLine = true,
                trailingIcon = {
                    if (ui.standardInput.isNotEmpty()) IconButton(onClick = { vm.setStandard("") }) { Icon(Icons.Default.Close, contentDescription = "Clear topic or standard") }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            // Optional curriculum code, like the web app's Standard field (e.g. NGSS HS-LS1-4, CCSS 4.NF.A.2, ELAGSE4RL2).
            OutlinedTextField(
                value = ui.standardCode,
                onValueChange = vm::setStandardCode,
                label = { Text("Standard / Curriculum Code (optional)") },
                placeholder = { Text("HS-LS1-4, 4.NF.A.2 or ELAGSE4RL2") },
                supportingText = { Text("Used exactly as typed if it isn't in the built-in catalog.") },
                singleLine = true,
                trailingIcon = {
                    if (ui.standardCode.isNotEmpty()) IconButton(onClick = { vm.setStandardCode("") }) { Icon(Icons.Default.Close, contentDescription = "Clear standard code") }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            // Optional: the book the lesson is built on. A title typed in the box above ("The Outsiders by S. E. Hinton") is picked up too.
            OutlinedTextField(
                value = ui.bookTitle,
                onValueChange = vm::setBook,
                label = { Text("Book title (optional)") },
                placeholder = { Text("The Outsiders") },
                supportingText = { Text("Any book works with any grade: the plan is scaled to the grade you pick. Type any title; catalog matches appear below.") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            // Catalog matches as you type. Any other title is accepted as typed: the plan names it and asks for your own passages.
            val matches = com.lessoncraft.engine.BookCatalog.suggest(ui.bookTitle)
            if (matches.isNotEmpty() && matches.none { it.title.equals(ui.bookTitle.trim(), ignoreCase = true) }) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    matches.forEach { b -> SuggestionChip(onClick = { vm.setBook(b.title) }, label = { Text(b.title, maxLines = 1) }) }
                }
            }

            // Large, conversational: the lesson's vision and the students' needs.
            OutlinedTextField(
                value = ui.customPrompt,
                onValueChange = vm::setCustom,
                label = { Text("Custom instructions (optional)") },
                placeholder = { Text("Describe your vision in your own words: a theme, how you teach, what your students need.\n\nFor example: A fall harvest unit for a class with several English learners and two students who need movement breaks.") },
                supportingText = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Use either box or both.", Modifier.weight(1f))
                        Text("${ui.customPrompt.length}/$CUSTOM_MAX")
                    }
                },
                minLines = 6,
                maxLines = 12,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            )

            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!ui.canSubmit) Text("Add a topic, standard or book, or describe what you want.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = { focus.clearFocus(); vm.getIdeas(again = false, onReady = onIdeas) },
                enabled = ui.canSubmit && !ui.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                if (ui.busy) CircularProgressIndicator(Modifier.padding(2.dp), strokeWidth = 2.dp) else Text("Get 3 ideas")
            }
        }
    }
}
