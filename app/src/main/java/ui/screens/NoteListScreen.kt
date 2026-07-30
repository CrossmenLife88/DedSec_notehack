package com.example.dedsec.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dedsec.data.Note
import com.example.dedsec.data.previewText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    notes: List<Note>,
    onAddClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onTogglePin: (Note) -> Unit
) {
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showHackScreen by remember { mutableStateOf(false) }
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        ">> DEDSEC_NOTES.SYS",
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime > 1500) tapCount = 0
                            lastTapTime = now
                            tapCount++
                            if (tapCount >= 5) {
                                tapCount = 0
                                showHackScreen = true

                            }
                        }
                    )
                })
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onAddClick() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(">> [+ NEW_FILE]")
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).padding(12.dp)) {
                items(notes) { note ->
                    val accentColor =
                        if (note.isPinned) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, accentColor)
                            .background(MaterialTheme.colorScheme.surface)
                            .combinedClickable(
                                onClick = { onNoteClick(note) },
                                onLongClick = { selectedNote = note }
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                (if (note.isPinned) "★ " else "") + "[FILE] >> ${note.title.ifBlank { "NO_NAME" }}",
                                color = accentColor
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(previewText(note.blocksRaw), color = accentColor)
                        }
                    }
                }
            }
        }

        // Всплывающее окно действий поверх экрана, без затемнения фона
        selectedNote?.let { note ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { selectedNote = null }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* съедаем клик, чтобы не закрывалось */ }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                ">> ACTIONS",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                "[X]",
                                color = Color.White,
                                modifier = Modifier
                                    .clickable { selectedNote = null }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTogglePin(note)
                                    selectedNote = null
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (note.isPinned) "UNSTAR" else "STAR",
                                color = Color(0xFFFFD700)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDeleteNote(note)
                                    selectedNote = null
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFFF003C)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("DELETE", color = Color(0xFFFF003C))
                        }
                    }
                }
            }
        }

        if (showHackScreen) {
            HackVisualScreen(onClose = { showHackScreen = false })
        }
    }
}