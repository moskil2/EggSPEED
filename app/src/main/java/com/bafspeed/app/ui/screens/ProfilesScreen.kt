package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

@Composable
fun ProfilesScreen(
    state: UiState,
    onSaveNew: (String) -> Unit,
    onLoad: (String) -> Unit,
    onDelete: (String) -> Unit,
    onImportFile: () -> Unit,
    onExportFile: () -> Unit,
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MicroLabel(tr("Zapisane w aplikacji", "Saved in the app"))
        if (state.profiles.isEmpty()) {
            TokenCard(borderColor = Tokens.WhiteBorder) {
                Text(tr("Brak zapisanych profili. Użyj przycisku „Zapisz bieżący”.", "No saved profiles yet. Use the \"Save current\" button."), fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
            }
        } else {
            state.profiles.forEach { name ->
                TokenCard(modifier = Modifier.clickable { onLoad(name) }, contentPadding = 14.dp, borderColor = Tokens.WhiteBorder) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(Tokens.BlueFaint16, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("≡", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Tokens.Blue)
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Tokens.TextPrimary)
                            Text(tr("Dotknij, aby wczytać (podgląd)", "Tap to load (preview)"), fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextSecondary)
                        }
                        Box(
                            Modifier.clickable { onDelete(name) }.padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(tr("Usuń", "Delete"), fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.Red)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Akcje plikowe
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedAction(tr("Importuj .ini", "Import .ini"), Modifier.weight(1f), onImportFile)
            FilledAction(tr("Zapisz bieżący", "Save current"), Modifier.weight(1f)) { newName = ""; showSaveDialog = true }
        }
        OutlinedAction(tr("Eksportuj do pliku", "Export to file"), Modifier.fillMaxWidth(), onExportFile)
        Spacer(Modifier.height(8.dp))
    }

    if (showSaveDialog) {
        AlertDialog(
            containerColor = Tokens.Card,
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                TextButton(onClick = { onSaveNew(newName); showSaveDialog = false }) {
                    Text(tr("Zapisz", "Save"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(tr("Anuluj", "Cancel"), color = Tokens.TextSecondary, fontFamily = Manrope)
                }
            },
            title = { Text(tr("Nazwa profilu", "Profile name"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
            text = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Tokens.Elevated, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    BasicTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Manrope, fontSize = 15.sp),
                        cursorBrush = SolidColor(Tokens.Blue),
                    )
                    if (newName.isEmpty()) {
                        Text(tr("np. Miasto 25 km/h", "e.g. City 25 km/h"), fontFamily = Manrope, fontSize = 15.sp, color = Tokens.TextSecondary)
                    }
                }
            },
        )
    }
}

@Composable
private fun FilledAction(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(Tokens.Blue, RoundedCornerShape(15.dp))
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.OnAccent)
    }
}

@Composable
private fun OutlinedAction(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(Tokens.Card, RoundedCornerShape(15.dp))
            .border(1.dp, Tokens.WhiteBorder, RoundedCornerShape(15.dp))
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}
