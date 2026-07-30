package com.example.dedsec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dedsec.data.NoteDatabase
import com.example.dedsec.ui.screens.NoteEditScreen
import com.example.dedsec.ui.screens.NoteListScreen
import com.example.dedsec.ui.theme.DedSecTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = NoteDatabase.getInstance(applicationContext)

        setContent {
            DedSecTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        val notes by db.noteDao().getAll().collectAsStateWithLifecycle(initialValue = emptyList())
                        val scope = rememberCoroutineScope()

                        NoteListScreen(
                            notes = notes,
                            onAddClick = { navController.navigate("edit/-1") },
                            onNoteClick = { note -> navController.navigate("edit/${note.id}") },
                            onDeleteNote = { note ->
                                scope.launch { db.noteDao().delete(note) }
                            },
                            onTogglePin = { note ->
                                scope.launch { db.noteDao().setPinned(note.id, !note.isPinned) }
                            }
                        )
                    }
                    composable(
                        route = "edit/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                        NoteEditScreen(
                            noteId = noteId,
                            database = db,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}