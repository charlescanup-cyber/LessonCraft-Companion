package com.lessoncraft.companion.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lessoncraft.companion.AppContainer
import com.lessoncraft.companion.ui.create.CreateScreen
import com.lessoncraft.companion.ui.create.CreateViewModel
import com.lessoncraft.companion.ui.create.IdeasScreen
import com.lessoncraft.companion.ui.detail.DetailViewModel
import com.lessoncraft.companion.ui.detail.LessonDetailScreen
import com.lessoncraft.companion.ui.library.LibraryScreen
import com.lessoncraft.companion.ui.library.LibraryViewModel

private const val CREATE = "create"
private const val IDEAS = "ideas"
private const val LIBRARY = "library"
private const val LESSON = "lesson/{id}"

/** Two tabs (Create, Library) and two pushed screens (the three ideas, one lesson). */
@Composable
fun CompanionNav(container: AppContainer) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    // Activity-scoped so the form and the session's idea history survive moving between tabs.
    val createVm: CreateViewModel = viewModel(factory = viewModelFactory { initializer { CreateViewModel(container) } })
    val libraryVm: LibraryViewModel = viewModel(factory = viewModelFactory { initializer { LibraryViewModel(container) } })

    Scaffold(
        bottomBar = {
            if (route == CREATE || route == LIBRARY) {
                NavigationBar {
                    listOf(CREATE to "Create", LIBRARY to "Library").forEach { (r, label) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = { nav.navigate(r) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                            icon = { Text(if (r == CREATE) "➕" else "📚") },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = CREATE, modifier = Modifier.padding(padding)) {
            composable(CREATE) { CreateScreen(createVm, onIdeas = { nav.navigate(IDEAS) }) }
            composable(IDEAS) {
                IdeasScreen(createVm, onBack = { nav.popBackStack() }, onLesson = { id ->
                    nav.navigate("lesson/$id") { popUpTo(CREATE) }
                })
            }
            composable(LIBRARY) { LibraryScreen(libraryVm, onOpen = { nav.navigate("lesson/$it") }) }
            composable(LESSON, arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: 0L
                val vm: DetailViewModel = viewModel(key = "lesson-$id", factory = viewModelFactory { initializer { DetailViewModel(container, id) } })
                LessonDetailScreen(vm, onClosed = { nav.popBackStack() })
            }
        }
    }
}
