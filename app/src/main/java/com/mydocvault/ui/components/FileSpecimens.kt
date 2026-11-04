package com.mydocvault.ui.components

import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun FontSpecimen(file: File, onOpen: () -> Unit) {
    val font by produceState<Result<Typeface>?>(null, file) {
        value = withContext(Dispatchers.IO) { runCatching { Typeface.createFromFile(file) } }
    }
    if (font == null) { ReaderLoading(); return }
    val face = font?.getOrNull()
    if (face == null) { ReaderMessage("This font could not be displayed.", "Choose another app", onOpen); return }
    val family = FontFamily(face)
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Type specimen", style = MaterialTheme.typography.labelLarge) }
        item { Text("Aa", fontFamily = family, fontSize = 72.sp, lineHeight = 80.sp) }
        items(listOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz", "0123456789", "The quick brown fox jumps over the lazy dog.")) {
            Text(it, fontFamily = family, fontSize = 26.sp, lineHeight = 36.sp)
        }
    }
}

@Composable
internal fun DatabaseIndex(file: File, onOpen: () -> Unit) {
    val schema by produceState<Result<List<Pair<String, String>>>?>(null, file) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                SQLiteDatabase.openDatabase(file.path, null,
                    SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS,
                    { /* A corrupt imported database must never be deleted by the platform handler. */ }).use { db ->
                    db.rawQuery("SELECT name, substr(sql,1,4096) FROM sqlite_master WHERE type IN ('table','view') ORDER BY name LIMIT 500", null).use { cursor ->
                        buildList { while (cursor.moveToNext()) add(cursor.getString(0) to cursor.getString(1).orEmpty()) }
                    }
                }
            }
        }
    }
    val result = schema
    if (result == null) { ReaderLoading(); return }
    val tables = result.getOrNull()
    if (tables == null) { ReaderMessage("This database could not be opened read-only.", "Choose another app", onOpen); return }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Tables and views", style = MaterialTheme.typography.headlineSmall) }
        if (tables.isEmpty()) item { Text("This database has no tables or views.") }
        items(tables) { (name, definition) ->
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(definition, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
        }
    }
}
