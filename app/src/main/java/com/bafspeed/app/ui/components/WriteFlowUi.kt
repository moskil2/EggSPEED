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
        title = { Text(tr("Zapisać do sterownika?", "Save to controller?", de = "An Steuergerät senden?", fr = "Enregistrer sur le contrôleur ?", es = "¿Guardar en el controlador?", pt = "Guardar no controlador?", it = "Salvare nel controller?", nl = "Opslaan naar controller?", sv = "Spara till styrenheten?", cs = "Uložit do řadiče?", sk = "Uložiť do radiča?"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                Text(
                    tr(
                        "Poniższe wartości zostaną wysłane i zapisane w pamięci sterownika:",
                        "The values below will be sent and saved in the controller's memory:",
                        de = "Die folgenden Werte werden gesendet und im Speicher des Steuergeräts gespeichert:",
                        fr = "Les valeurs ci-dessous seront envoyées et enregistrées dans la mémoire du contrôleur :",
                        es = "Los siguientes valores se enviarán y guardarán en la memoria del controlador:",
                        pt = "Os valores abaixo serão enviados e guardados na memória do controlador:",
                        it = "I valori sottostanti verranno inviati e salvati nella memoria del controller:",
                        nl = "De onderstaande waarden worden verzonden en opgeslagen in het geheugen van de controller:",
                        sv = "Värdena nedan skickas och sparas i styrenhetens minne:",
                        cs = "Níže uvedené hodnoty budou odeslány a uloženy v paměti řadiče:",
                        sk = "Nižšie uvedené hodnoty budú odoslané a uložené v pamäti radiča:",
                    ),
                    fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary,
                )
                Spacer(Modifier.height(10.dp))
                flow.changes.forEach { change ->
                    Text("• $change", fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    tr("PODGLĄD RAMEK (dry-run)", "FRAME PREVIEW (dry-run)", de = "RAHMEN-VORSCHAU (dry-run)", fr = "APERÇU DES TRAMES (dry-run)", es = "VISTA PREVIA DE TRAMAS (dry-run)", pt = "PRÉ-VISUALIZAÇÃO DE TRAMAS (dry-run)", it = "ANTEPRIMA FRAME (dry-run)", nl = "FRAME-VOORBEELD (dry-run)", sv = "RAMFÖRHANDSVISNING (dry-run)", cs = "NÁHLED RÁMCŮ (dry-run)", sk = "NÁHĽAD RÁMCOV (dry-run)"),
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
                Text(tr("Wyślij do sterownika", "Send to controller", de = "An Steuergerät senden", fr = "Envoyer au contrôleur", es = "Enviar al controlador", pt = "Enviar para o controlador", it = "Invia al controller", nl = "Verzenden naar controller", sv = "Skicka till styrenheten", cs = "Odeslat do řadiče", sk = "Odoslať do radiča"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(tr("Anuluj", "Cancel", de = "Abbrechen", fr = "Annuler", es = "Cancelar", pt = "Cancelar", it = "Annulla", nl = "Annuleren", sv = "Avbryt", cs = "Zrušit", sk = "Zrušiť"), color = Tokens.TextTertiary, fontFamily = Manrope) }
        },
    )
}

@Composable
private fun ProgressDialog(step: String) {
    AlertDialog(
        containerColor = Tokens.Card,
        onDismissRequest = {},
        confirmButton = {},
        title = { Text(tr("Zapisywanie…", "Saving…", de = "Speichern…", fr = "Enregistrement…", es = "Guardando…", pt = "A guardar…", it = "Salvataggio…", nl = "Opslaan…", sv = "Sparar…", cs = "Ukládám…", sk = "Ukladám…"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
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
                if (success) {
                    tr("Zapisano", "Saved", de = "Gespeichert", fr = "Enregistré", es = "Guardado", pt = "Guardado", it = "Salvato", nl = "Opgeslagen", sv = "Sparat", cs = "Uloženo", sk = "Uložené")
                } else {
                    tr("Coś poszło nie tak", "Something went wrong", de = "Etwas ist schiefgelaufen", fr = "Une erreur s'est produite", es = "Algo salió mal", pt = "Algo correu mal", it = "Qualcosa è andato storto", nl = "Er is iets misgegaan", sv = "Något gick fel", cs = "Něco se pokazilo", sk = "Niečo sa pokazilo")
                },
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
