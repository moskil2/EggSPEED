package com.bafspeed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FramePreview
import com.bafspeed.app.WriteFlow
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/** Dialogi przepływu zapisu: potwierdzenie (z dry-run), postęp, wynik. Renderuje się automatycznie nad wszystkim. */
@Composable
fun WriteFlowDialogs(
    flow: WriteFlow,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onAcknowledge: () -> Unit,
) {
    when (flow) {
        WriteFlow.Idle -> Unit
        is WriteFlow.Confirming -> ConfirmDialog(flow, onConfirm, onCancel)
        is WriteFlow.InProgress -> ProgressDialog(flow.step)
        is WriteFlow.Done -> ResultDialog(flow.success, flow.message, onAcknowledge)
    }
}

@Composable
private fun ConfirmDialog(flow: WriteFlow.Confirming, onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        containerColor = Tokens.Card,
        onDismissRequest = onCancel,
        title = { Text(tr("Zapisać do sterownika?", "Save to controller?"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                Text(
                    tr("Poniższe wartości zostaną wysłane i zapisane w pamięci sterownika:", "The values below will be sent and saved in the controller's memory:"),
                    fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary,
                )
                Spacer(Modifier.height(10.dp))
                flow.changes.forEach { change ->
                    Text("• $change", fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    tr("PODGLĄD RAMEK (dry-run)", "FRAME PREVIEW (dry-run)"),
                    fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 11.sp,
                    letterSpacing = 1.sp, color = Tokens.TextTertiary,
                )
                Spacer(Modifier.height(6.dp))
                flow.frames.forEach { fp: FramePreview ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Tokens.Elevated, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                    ) {
                        Column {
                            Text(fp.blockName, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Tokens.Blue)
                            Text(fp.hex, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Tokens.TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(tr("Wyślij do sterownika", "Send to controller"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(tr("Anuluj", "Cancel"), color = Tokens.TextTertiary, fontFamily = Manrope) }
        },
    )
}

@Composable
private fun ProgressDialog(step: String) {
    AlertDialog(
        containerColor = Tokens.Card,
        onDismissRequest = {},
        confirmButton = {},
        title = { Text(tr("Zapisywanie…", "Saving…"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Tokens.Blue, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(step, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
            }
        },
    )
}

@Composable
private fun ResultDialog(success: Boolean, message: String, onAcknowledge: () -> Unit) {
    AlertDialog(
        containerColor = Tokens.Card,
        onDismissRequest = onAcknowledge,
        title = {
            Text(
                if (success) tr("Zapisano", "Saved") else tr("Coś poszło nie tak", "Something went wrong"),
                fontFamily = Sora, fontWeight = FontWeight.Bold,
                color = if (success) Tokens.Emerald else Tokens.Red,
            )
        },
        text = { Text(message, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary) },
        confirmButton = {
            TextButton(onClick = onAcknowledge) { Text("OK", color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold) }
        },
    )
}
