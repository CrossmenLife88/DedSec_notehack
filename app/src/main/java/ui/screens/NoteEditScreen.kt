package com.example.dedsec.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dedsec.data.Note
import com.example.dedsec.data.NoteBlock
import com.example.dedsec.data.NoteDatabase
import com.example.dedsec.data.TextSpan
import com.example.dedsec.data.decodeBlocks
import com.example.dedsec.data.encodeBlocks
import kotlinx.coroutines.launch
import java.io.File

private class EditableTextBlock(
    initialText: String,
    initialFontSize: Int,
    initialSpans: List<TextSpan>
) {
    var field by mutableStateOf(TextFieldValue(initialText))
    var fontSize by mutableStateOf(initialFontSize)
    var spans by mutableStateOf(initialSpans)
}

private sealed class EditableBlock {
    data class TextB(val state: EditableTextBlock) : EditableBlock()
    data class ImageB(val fileName: String) : EditableBlock()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Int,
    database: NoteDatabase,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var titleFontSize by remember { mutableStateOf(18) }
    var existingNote by remember { mutableStateOf<Note?>(null) }
    val blocks = remember { mutableStateListOf<EditableBlock>() }
    var showDrawing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val fileName = "photo_${System.currentTimeMillis()}.png"
                context.contentResolver.openInputStream(uri)?.use { input ->
                    File(context.filesDir, fileName).outputStream().use { output -> input.copyTo(output) }
                }
                blocks.add(EditableBlock.ImageB(fileName))
            }
        }
    }

    LaunchedEffect(noteId) {
        if (noteId != -1) {
            database.noteDao().getById(noteId)?.let { note ->
                existingNote = note
                title = note.title
                titleFontSize = note.titleFontSize
                decodeBlocks(note.blocksRaw).forEach { block ->
                    when (block) {
                        is NoteBlock.TextBlock -> blocks.add(
                            EditableBlock.TextB(EditableTextBlock(block.text, block.fontSize, block.spans))
                        )
                        is NoteBlock.ImageBlock -> blocks.add(EditableBlock.ImageB(block.fileName))
                    }
                }
            }
        }
        if (blocks.isEmpty()) {
            blocks.add(EditableBlock.TextB(EditableTextBlock("", 16, emptyList())))
        }
    }

    fun save() {
        scope.launch {
            val noteBlocks = blocks.map { block ->
                when (block) {
                    is EditableBlock.TextB -> NoteBlock.TextBlock(
                        block.state.field.text, block.state.fontSize, block.state.spans
                    )
                    is EditableBlock.ImageB -> NoteBlock.ImageBlock(block.fileName)
                }
            }
            val blocksRaw = encodeBlocks(noteBlocks)
            val hasContent = title.isNotBlank() || noteBlocks.any {
                (it is NoteBlock.TextBlock && it.text.isNotBlank()) || it is NoteBlock.ImageBlock
            }
            if (existingNote != null) {
                database.noteDao().update(
                    existingNote!!.copy(title = title, titleFontSize = titleFontSize, blocksRaw = blocksRaw)
                )
            } else if (hasContent) {
                database.noteDao().insert(
                    Note(title = title, titleFontSize = titleFontSize, blocksRaw = blocksRaw)
                )
            }
            onBack()
        }
    }

    if (showDrawing) {
        DrawingOverlay(
            onSave = { fileName ->
                blocks.add(EditableBlock.ImageB(fileName))
                showDrawing = false
            },
            onClose = { showDrawing = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingNote != null) "[EDIT_FILE]" else "[NEW_FILE]") },
                navigationIcon = { TextButton(onClick = { save() }) { Text("< BACK") } },
                actions = { TextButton(onClick = { save() }) { Text("SAVE") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("[A-]", modifier = Modifier.clickable { if (titleFontSize > 12) titleFontSize -= 2 }.padding(6.dp))
                Text("TITLE SIZE: $titleFontSize", modifier = Modifier.padding(horizontal = 8.dp))
                Text("[A+]", modifier = Modifier.clickable { if (titleFontSize < 40) titleFontSize += 2 }.padding(6.dp))
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Заголовок") },
                singleLine = true,
                textStyle = TextStyle(fontSize = titleFontSize.sp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Column {
                blocks.forEachIndexed { index, block ->
                    when (block) {
                        is EditableBlock.TextB -> {
                            val state = block.state
                            val bringIntoViewRequester = remember { BringIntoViewRequester() }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("[A-]", modifier = Modifier.clickable { if (state.fontSize > 10) state.fontSize -= 2 }.padding(6.dp))
                                Text("SIZE: ${state.fontSize}", modifier = Modifier.padding(horizontal = 8.dp))
                                Text("[A+]", modifier = Modifier.clickable { if (state.fontSize < 36) state.fontSize += 2 }.padding(6.dp))
                                Text("[SEL-]", modifier = Modifier.clickable {
                                    val sel = state.field.selection
                                    if (!sel.collapsed) {
                                        val cur = state.spans.find { it.start == sel.min && it.end == sel.max }?.fontSize ?: state.fontSize
                                        state.spans = state.spans.filterNot { it.start < sel.max && it.end > sel.min } +
                                                TextSpan(sel.min, sel.max, (cur - 2).coerceAtLeast(10))
                                    }
                                }.padding(6.dp))
                                Text("[SEL+]", modifier = Modifier.clickable {
                                    val sel = state.field.selection
                                    if (!sel.collapsed) {
                                        val cur = state.spans.find { it.start == sel.min && it.end == sel.max }?.fontSize ?: state.fontSize
                                        state.spans = state.spans.filterNot { it.start < sel.max && it.end > sel.min } +
                                                TextSpan(sel.min, sel.max, (cur + 2).coerceAtMost(48))
                                    }
                                }.padding(6.dp))
                                if (blocks.size > 1) {
                                    Text("[remove]", color = Color(0xFFFF003C), modifier = Modifier.clickable { blocks.removeAt(index) }.padding(6.dp))
                                }
                            }

                            val visualTransformation = remember(state.spans) {
                                VisualTransformation { text ->
                                    val annotated = buildAnnotatedString {
                                        append(text)
                                        state.spans.forEach { span ->
                                            if (span.start in 0..text.length && span.end in span.start..text.length) {
                                                addStyle(SpanStyle(fontSize = span.fontSize.sp), span.start, span.end)
                                            }
                                        }
                                    }
                                    TransformedText(annotated, OffsetMapping.Identity)
                                }
                            }

                            TextField(
                                value = state.field,
                                onValueChange = {
                                    state.field = it
                                    scope.launch { bringIntoViewRequester.bringIntoView() }
                                },
                                placeholder = { Text("Текст заметки...") },
                                textStyle = TextStyle(fontSize = state.fontSize.sp),
                                visualTransformation = visualTransformation,
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 100.dp)
                                    .bringIntoViewRequester(bringIntoViewRequester)
                                    .onFocusEvent { focusState ->
                                        if (focusState.isFocused) {
                                            scope.launch { bringIntoViewRequester.bringIntoView() }
                                        }
                                    }
                            )
                        }

                        is EditableBlock.ImageB -> {
                            Spacer(Modifier.height(12.dp))
                            val bitmap = remember(block.fileName) {
                                BitmapFactory.decodeFile(File(context.filesDir, block.fileName).absolutePath)?.asImageBitmap()
                            }
                            bitmap?.let {
                                Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxWidth())
                            }
                            Text(
                                "[remove image]",
                                color = Color(0xFFFF003C),
                                modifier = Modifier.clickable { blocks.removeAt(index) }.padding(top = 4.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("[+ TEXT]", modifier = Modifier.clickable {
                    blocks.add(EditableBlock.TextB(EditableTextBlock("", 16, emptyList())))
                }.padding(8.dp))
                Text("[+ PHOTO]", modifier = Modifier.clickable {
                    pickImageLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }.padding(8.dp))
                Text("[+ DRAWING]", modifier = Modifier.clickable {
                    showDrawing = true
                }.padding(8.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}